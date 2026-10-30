package com.example.viewmodel

import android.app.Activity
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.StarredMessageEntity
import com.example.data.repository.ContactItem
import com.example.data.repository.GatewayRepository
import com.example.data.repository.SmsRepository
import com.example.model.ApiKeyItem
import com.example.model.BusinessAccount
import com.example.model.CategoryTab
import com.example.model.GatewayDispatch
import com.example.model.NavigationTab
import com.example.model.SmsMessage
import com.example.model.SmsThread
import com.example.notification.PulseNotificationHelper
import com.example.receiver.SmsReceiver
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed class ScreenDestination {
    object Inbox : ScreenDestination()
    data class ChatThread(val threadId: Long, val address: String, val displayName: String) : ScreenDestination()
    object Compose : ScreenDestination()
    object Search : ScreenDestination()
    object Saved : ScreenDestination()
    object Settings : ScreenDestination()
    object Gateway : ScreenDestination()
}

class PulseViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = SmsRepository(application)
    private val gatewayRepository = GatewayRepository(application)

    // Permission state
    private val _hasPermissions = MutableStateFlow(false)
    val hasPermissions: StateFlow<Boolean> = _hasPermissions.asStateFlow()

    // Screen destination
    private val _currentScreen = MutableStateFlow<ScreenDestination>(ScreenDestination.Inbox)
    val currentScreen: StateFlow<ScreenDestination> = _currentScreen.asStateFlow()

    // Navigation Tab
    private val _currentNavTab = MutableStateFlow(NavigationTab.INBOX)
    val currentNavTab: StateFlow<NavigationTab> = _currentNavTab.asStateFlow()

    // Category Tab
    private val _selectedCategory = MutableStateFlow(CategoryTab.ALL)
    val selectedCategory: StateFlow<CategoryTab> = _selectedCategory.asStateFlow()

    // Search Query
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Raw conversations from repository ContentObserver
    private val _rawConversations = MutableStateFlow<List<SmsThread>>(emptyList())
    val allConversations: StateFlow<List<SmsThread>> = _rawConversations.asStateFlow()

    // Gateway State
    val currentUser: StateFlow<FirebaseUser?> = gatewayRepository.observeAuthState()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), gatewayRepository.currentUser)

    val businessAccount: StateFlow<BusinessAccount?> = gatewayRepository.observeBusinessAccount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val apiKeys: StateFlow<List<ApiKeyItem>> = gatewayRepository.observeApiKeys()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val gatewayDispatches: StateFlow<List<GatewayDispatch>> = gatewayRepository.observeLiveDispatches()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Dynamic Category Counts
    val categoryCounts: StateFlow<Map<CategoryTab, Int>> = _rawConversations.map { threads ->
        val map = mutableMapOf<CategoryTab, Int>()
        val unarchived = threads.filter { !it.isArchived }
        map[CategoryTab.ALL] = unarchived.size
        map[CategoryTab.PERSONAL] = unarchived.count { it.category == CategoryTab.PERSONAL }
        map[CategoryTab.TRANSACTIONS] = unarchived.count { it.category == CategoryTab.TRANSACTIONS }
        map[CategoryTab.UPDATES] = unarchived.count { it.category == CategoryTab.UPDATES }
        map[CategoryTab.ARCHIVED] = threads.count { it.isArchived }
        map
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    // Total Unread Count for bottom navigation badge
    val totalUnreadCount: StateFlow<Int> = _rawConversations.map { threads ->
        threads.sumOf { it.unreadCount }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // Filtered conversations for Inbox
    val conversations: StateFlow<List<SmsThread>> = combine(
        _rawConversations,
        _selectedCategory
    ) { threads, category ->
        var list = threads

        if (category == CategoryTab.ARCHIVED) {
            list = list.filter { it.isArchived }
        } else {
            list = list.filter { !it.isArchived }
            if (category != CategoryTab.ALL) {
                list = list.filter { it.category == category }
            }
        }

        list
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Active Thread messages
    private val _currentThreadMessages = MutableStateFlow<List<SmsMessage>>(emptyList())
    val currentThreadMessages: StateFlow<List<SmsMessage>> = _currentThreadMessages.asStateFlow()

    // Starred messages
    val starredMessages: StateFlow<List<StarredMessageEntity>> = repository.observeStarredMessages()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Contact search results for compose
    private val _contactSuggestions = MutableStateFlow<List<ContactItem>>(emptyList())
    val contactSuggestions: StateFlow<List<ContactItem>> = _contactSuggestions.asStateFlow()

    // Sending indicator
    private val _isSending = MutableStateFlow(false)
    val isSending: StateFlow<Boolean> = _isSending.asStateFlow()

    // Feedback message
    private val _feedbackMessage = MutableStateFlow<String?>(null)
    val feedbackMessage: StateFlow<String?> = _feedbackMessage.asStateFlow()

    init {
        // Observe real-time incoming SMS broadcasts
        viewModelScope.launch {
            SmsReceiver.liveIncomingSms.collect { event ->
                _feedbackMessage.value = "New SMS from ${repository.resolveName(event.sender)}"
                loadConversations()
                val screen = _currentScreen.value
                if (screen is ScreenDestination.ChatThread) {
                    loadMessagesForThread(screen.threadId)
                }
            }
        }

        // Track already notified dispatches to prevent duplicate alerts
        val notifiedDispatchIds = mutableSetOf<String>()

        // Observe real-time Gateway Dispatches from Firestore and inject into conversations
        viewModelScope.launch {
            gatewayDispatches.collect { dispatches ->
                if (dispatches.isNotEmpty()) {
                    val now = System.currentTimeMillis()
                    dispatches.forEach { dispatch ->
                        val createdTime = dispatch.createdAt?.toDate()?.time ?: now
                        if (dispatch.dispatchId.isNotBlank() &&
                            !notifiedDispatchIds.contains(dispatch.dispatchId) &&
                            (now - createdTime) < 45000L
                        ) {
                            notifiedDispatchIds.add(dispatch.dispatchId)
                            _feedbackMessage.value = "Gateway OTP Received from ${dispatch.senderName}: ${dispatch.otpCode}"
                            
                            // Real Android high-priority notification with one-tap Copy OTP action!
                            PulseNotificationHelper.showMessageNotification(
                                context = getApplication(),
                                senderName = dispatch.senderName,
                                address = dispatch.recipientPhone,
                                body = dispatch.body,
                                threadId = -(dispatch.senderName.hashCode().toLong().coerceAtLeast(1L))
                            )
                        }
                    }

                    mergeGatewayDispatchesIntoInbox(dispatches)

                    val screen = _currentScreen.value
                    if (screen is ScreenDestination.ChatThread && screen.threadId <= 0) {
                        updateGatewayThreadMessages(screen.threadId, screen.address)
                    }
                }
            }
        }
    }

    private fun mergeGatewayDispatchesIntoInbox(dispatches: List<GatewayDispatch>) {
        if (dispatches.isEmpty()) return
        val current = _rawConversations.value.toMutableList()

        dispatches.forEach { dispatch ->
            val syntheticId = -(dispatch.senderName.hashCode().toLong().coerceAtLeast(1L))
            val existingIdx = current.indexOfFirst { it.displayName.startsWith(dispatch.senderName) || it.threadId == syntheticId }

            val threadItem = SmsThread(
                threadId = syntheticId,
                address = dispatch.recipientPhone,
                displayName = "${dispatch.senderName} (Gateway OTP)",
                snippet = dispatch.body,
                date = dispatch.createdAt?.toDate()?.time ?: System.currentTimeMillis(),
                unreadCount = 1,
                messageCount = 1,
                isPinned = false,
                isArchived = false,
                category = CategoryTab.TRANSACTIONS
            )

            if (existingIdx != -1) {
                current[existingIdx] = threadItem
            } else {
                current.add(0, threadItem)
            }
        }
        _rawConversations.value = current
    }

    fun onPermissionsResult(granted: Boolean) {
        _hasPermissions.value = granted
        if (granted) {
            startObservingConversations()
        }
    }

    private fun startObservingConversations() {
        viewModelScope.launch {
            repository.observeConversations().collect { threads ->
                _rawConversations.value = threads
            }
        }
    }

    fun loadConversations() {
        viewModelScope.launch {
            _rawConversations.value = repository.getConversations()
        }
    }

    fun selectThread(thread: SmsThread) {
        _currentScreen.value = ScreenDestination.ChatThread(
            threadId = thread.threadId,
            address = thread.address,
            displayName = thread.displayName
        )
        if (thread.threadId > 0) {
            loadMessagesForThread(thread.threadId)
            viewModelScope.launch {
                repository.markThreadAsRead(thread.threadId)
                loadConversations()
            }
        } else {
            // Gateway Synthetic Thread - load all dispatches from this sender
            updateGatewayThreadMessages(thread.threadId, thread.address)
        }
    }

    private fun updateGatewayThreadMessages(threadId: Long, address: String) {
        val matchingDispatches = gatewayDispatches.value.filter {
            val syntheticId = -(it.senderName.hashCode().toLong().coerceAtLeast(1L))
            syntheticId == threadId || it.recipientPhone == address
        }.sortedBy { it.createdAt?.toDate()?.time ?: 0L }

        if (matchingDispatches.isNotEmpty()) {
            _currentThreadMessages.value = matchingDispatches.mapIndexed { idx, disp ->
                val dateMs = disp.createdAt?.toDate()?.time ?: (System.currentTimeMillis() - (matchingDispatches.size - idx) * 60000L)
                SmsMessage(
                    id = disp.dispatchId.hashCode().toLong(),
                    threadId = threadId,
                    address = disp.recipientPhone,
                    body = disp.body,
                    date = dateMs,
                    type = 1, // Inbox
                    read = true
                )
            }
        }
    }

    fun openThreadDirect(address: String, name: String) {
        val existing = _rawConversations.value.find { it.address == address }
        val threadId = existing?.threadId ?: 0L
        _currentScreen.value = ScreenDestination.ChatThread(
            threadId = threadId,
            address = address,
            displayName = name.ifBlank { repository.resolveName(address) }
        )
        if (threadId > 0) {
            loadMessagesForThread(threadId)
        } else {
            _currentThreadMessages.value = emptyList()
        }
    }

    fun closeThread() {
        _currentScreen.value = when (_currentNavTab.value) {
            NavigationTab.INBOX -> ScreenDestination.Inbox
            NavigationTab.SAVED -> ScreenDestination.Saved
            NavigationTab.SETTINGS -> ScreenDestination.Settings
        }
    }

    fun openCompose() {
        _currentScreen.value = ScreenDestination.Compose
        searchContacts("")
    }

    fun openSearch() {
        _currentScreen.value = ScreenDestination.Search
    }

    fun openGateway() {
        _currentScreen.value = ScreenDestination.Gateway
    }

    fun closeSearch() {
        _searchQuery.value = ""
        closeThread()
    }

    fun navigateToNavTab(tab: NavigationTab) {
        _currentNavTab.value = tab
        _currentScreen.value = when (tab) {
            NavigationTab.INBOX -> ScreenDestination.Inbox
            NavigationTab.SAVED -> ScreenDestination.Saved
            NavigationTab.SETTINGS -> ScreenDestination.Settings
        }
    }

    fun selectNavTab(tab: NavigationTab) = navigateToNavTab(tab)

    fun setCategory(category: CategoryTab) {
        _selectedCategory.value = category
    }

    fun selectCategory(category: CategoryTab) = setCategory(category)

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun searchContacts(query: String) {
        viewModelScope.launch {
            _contactSuggestions.value = repository.searchContacts(query)
        }
    }

    fun loadMessagesForThread(threadId: Long) {
        viewModelScope.launch {
            repository.observeMessagesForThread(threadId).collect { msgs ->
                _currentThreadMessages.value = msgs
            }
        }
    }

    fun sendSms(destination: String, text: String, threadId: Long? = null) {
        if (destination.isBlank() || text.isBlank()) return

        viewModelScope.launch {
            _isSending.value = true
            val success = repository.sendSms(destination, text, threadId)
            _isSending.value = false

            if (success) {
                _feedbackMessage.value = "Message sent"
                loadConversations()
                if (threadId != null && threadId > 0) {
                    loadMessagesForThread(threadId)
                }
            } else {
                _feedbackMessage.value = "Failed to send message. Check network or carrier."
            }
        }
    }

    fun togglePin(threadId: Long, isPinned: Boolean) {
        viewModelScope.launch {
            repository.togglePinThread(threadId, isPinned)
            loadConversations()
        }
    }

    fun toggleArchive(threadId: Long, isArchived: Boolean) {
        viewModelScope.launch {
            repository.toggleArchiveThread(threadId, isArchived)
            loadConversations()
        }
    }

    fun toggleStar(message: SmsMessage) {
        viewModelScope.launch {
            repository.toggleStarMessage(message)
            val screen = _currentScreen.value
            if (screen is ScreenDestination.ChatThread) {
                loadMessagesForThread(screen.threadId)
            }
        }
    }

    // Business Gateway actions
    fun signInAndConvertBusiness(activity: Activity, orgName: String) {
        viewModelScope.launch {
            gatewayRepository.signInWithGoogle(
                activity = activity,
                onSuccess = { user ->
                    viewModelScope.launch {
                        val success = gatewayRepository.convertToBusinessAccount(orgName)
                        if (success) {
                            _feedbackMessage.value = "Account converted to Business Gateway for $orgName!"
                            _currentScreen.value = ScreenDestination.Gateway
                        } else {
                            _feedbackMessage.value = "Failed to create Business Profile"
                        }
                    }
                },
                onError = { error ->
                    _feedbackMessage.value = "Google Sign-In: $error"
                }
            )
        }
    }

    fun generateApiKey(appName: String) {
        viewModelScope.launch {
            if (currentUser.value == null) {
                _feedbackMessage.value = "Please sign in from Settings to manage Business API keys"
                return@launch
            }
            val key = gatewayRepository.generateApiKey(appName)
            if (key != null) {
                _feedbackMessage.value = "Generated new API Key: $key"
            } else {
                _feedbackMessage.value = "Failed to generate key"
            }
        }
    }

    fun simulateGatewayDispatch(appName: String, phone: String, otp: String, apiKey: String) {
        viewModelScope.launch {
            if (currentUser.value == null) {
                _feedbackMessage.value = "Please sign in to a Business Account from Settings before dispatching"
                return@launch
            }
            val ok = gatewayRepository.simulateExternalAppDispatch(appName, phone, otp, apiKey)
            if (ok) {
                _feedbackMessage.value = "Dispatch sent through Gateway!"
            } else {
                _feedbackMessage.value = "Failed to dispatch: check connection or permissions"
            }
        }
    }

    fun signOutBusiness() {
        viewModelScope.launch {
            gatewayRepository.signOut()
            _feedbackMessage.value = "Signed out of developer account"
        }
    }

    fun clearFeedback() {
        _feedbackMessage.value = null
    }

    // Onboarding Management
    private val prefs = application.getSharedPreferences("pulse_prefs", android.content.Context.MODE_PRIVATE)
    private val _isOnboardingCompleted = MutableStateFlow(prefs.getBoolean("has_completed_onboarding", false))
    val isOnboardingCompleted: StateFlow<Boolean> = _isOnboardingCompleted.asStateFlow()

    fun completeOnboarding() {
        prefs.edit().putBoolean("has_completed_onboarding", true).apply()
        _isOnboardingCompleted.value = true
    }

    // Scheduled SMS
    val allPendingScheduled: StateFlow<List<com.example.data.local.ScheduledMessageEntity>> = repository.observeAllPendingScheduled()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun scheduleMessage(phone: String, name: String, body: String, timestamp: Long) {
        viewModelScope.launch {
            repository.scheduleMessage(phone, name, body, timestamp) {
                _feedbackMessage.value = "Message scheduled for delivery!"
            }
        }
    }

    fun cancelScheduledMessage(id: Long) {
        viewModelScope.launch {
            repository.cancelScheduledMessage(id)
            _feedbackMessage.value = "Scheduled message cancelled"
        }
    }

    // Spam & Blocked Numbers
    val blockedNumbers: StateFlow<List<com.example.data.local.BlockedNumberEntity>> = repository.observeBlockedNumbers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun blockNumber(phone: String, name: String) {
        viewModelScope.launch {
            repository.blockNumber(phone, name)
            _feedbackMessage.value = "Blocked $phone from inbox"
            loadConversations()
        }
    }

    fun unblockNumber(phone: String) {
        viewModelScope.launch {
            repository.unblockNumber(phone)
            _feedbackMessage.value = "Unblocked $phone"
            loadConversations()
        }
    }

    // Auto-Purge Expired OTPs
    fun purgeExpiredOtps(hours: Long = 24) {
        viewModelScope.launch {
            val count = repository.purgeExpiredOtps(hours)
            _feedbackMessage.value = if (count > 0) "Purged $count expired OTP messages" else "No expired OTPs found"
            loadConversations()
        }
    }

    // Backup & Export
    fun exportBackup(context: android.content.Context) {
        viewModelScope.launch {
            val json = com.example.data.repository.BackupHelper.exportToJson(_rawConversations.value)
            com.example.data.repository.BackupHelper.shareBackup(context, json)
        }
    }

    // OTP Code Verification Tester
    private val _verificationResult = MutableStateFlow<GatewayRepository.OtpVerificationResult?>(null)
    val verificationResult: StateFlow<GatewayRepository.OtpVerificationResult?> = _verificationResult.asStateFlow()

    fun verifyOtp(code: String, targetPhone: String? = null) {
        viewModelScope.launch {
            val result = gatewayRepository.verifyOtpCode(code, targetPhone)
            _verificationResult.value = result
            when (result) {
                is GatewayRepository.OtpVerificationResult.Success -> {
                    _feedbackMessage.value = "Verified! OTP for ${result.senderName} is valid (Latency: ${result.latencyMs}ms)"
                }
                is GatewayRepository.OtpVerificationResult.Expired -> {
                    _feedbackMessage.value = "Verification Failed: OTP code has expired (> 5 min window)"
                }
                is GatewayRepository.OtpVerificationResult.InvalidOrNotFound -> {
                    _feedbackMessage.value = "Verification Failed: Code not found or mismatch"
                }
                is GatewayRepository.OtpVerificationResult.Error -> {
                    _feedbackMessage.value = "Verification Error: ${result.message}"
                }
            }
        }
    }

    fun clearVerificationResult() {
        _verificationResult.value = null
    }
}
