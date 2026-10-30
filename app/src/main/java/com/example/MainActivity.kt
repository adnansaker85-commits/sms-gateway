package com.example

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.NavigationTab
import com.example.notification.PulseNotificationHelper
import com.example.ui.components.PulseFloatingNavBar
import com.example.ui.components.PulseMainTopBar
import com.example.ui.screens.ChatThreadScreen
import com.example.ui.screens.ComposeScreen
import com.example.ui.screens.ConversationListScreen
import com.example.ui.screens.GatewayScreen
import com.example.ui.screens.OnboardingScreen
import com.example.ui.screens.PermissionGateScreen
import com.example.ui.screens.PulseSearchScreen
import com.example.ui.screens.SavedMessagesScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.PorcelainWhite
import com.example.ui.theme.PulseTheme
import com.example.viewmodel.PulseViewModel
import com.example.viewmodel.ScreenDestination

class MainActivity : ComponentActivity() {

    private val viewModel: PulseViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        PulseNotificationHelper.createNotificationChannel(this)
        handleIncomingIntent(intent)

        setContent {
            PulseTheme {
                PulseApp(
                    viewModel = viewModel,
                    checkPermissions = { checkRequiredPermissions() },
                    checkNotificationPermission = { checkNotificationPermission() }
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIncomingIntent(intent)
    }

    private fun handleIncomingIntent(intent: Intent?) {
        if (intent == null) return
        val data = intent.data
        if (intent.action == Intent.ACTION_SENDTO && data != null) {
            val scheme = data.scheme
            if (scheme == "sms" || scheme == "smsto") {
                val recipient = data.schemeSpecificPart
                if (!recipient.isNullOrBlank()) {
                    viewModel.openThreadDirect(recipient, "")
                }
            }
        }

        // Notification deep-linking
        val notificationAddress = intent.getStringExtra(PulseNotificationHelper.EXTRA_ADDRESS)
        if (!notificationAddress.isNullOrBlank()) {
            viewModel.openThreadDirect(notificationAddress, "")
        }
    }

    private fun checkRequiredPermissions(): Boolean {
        val readSms = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS) == PackageManager.PERMISSION_GRANTED
        val sendSms = ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED
        return readSms && sendSms
    }

    private fun checkNotificationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }
}

@Composable
fun PulseApp(
    viewModel: PulseViewModel,
    checkPermissions: () -> Boolean,
    checkNotificationPermission: () -> Boolean
) {
    val context = LocalContext.current
    val hasPermissions by viewModel.hasPermissions.collectAsStateWithLifecycle()
    val isOnboardingCompleted by viewModel.isOnboardingCompleted.collectAsStateWithLifecycle()
    var hasNotificationPermission by remember { mutableStateOf(checkNotificationPermission()) }

    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val navTab by viewModel.currentNavTab.collectAsStateWithLifecycle()
    val category by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val conversations by viewModel.conversations.collectAsStateWithLifecycle()
    val allConversations by viewModel.allConversations.collectAsStateWithLifecycle()
    val categoryCounts by viewModel.categoryCounts.collectAsStateWithLifecycle()
    val totalUnreadCount by viewModel.totalUnreadCount.collectAsStateWithLifecycle()
    val currentMessages by viewModel.currentThreadMessages.collectAsStateWithLifecycle()
    val starredMessages by viewModel.starredMessages.collectAsStateWithLifecycle()
    val contactSuggestions by viewModel.contactSuggestions.collectAsStateWithLifecycle()
    val isSending by viewModel.isSending.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val feedbackMessage by viewModel.feedbackMessage.collectAsStateWithLifecycle()
    val blockedNumbers by viewModel.blockedNumbers.collectAsStateWithLifecycle()

    // Gateway states
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val businessAccount by viewModel.businessAccount.collectAsStateWithLifecycle()
    val apiKeys by viewModel.apiKeys.collectAsStateWithLifecycle()
    val gatewayDispatches by viewModel.gatewayDispatches.collectAsStateWithLifecycle()
    val verificationResult by viewModel.verificationResult.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        val granted = results[Manifest.permission.READ_SMS] == true &&
                results[Manifest.permission.SEND_SMS] == true
        viewModel.onPermissionsResult(granted)
    }

    val notificationLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasNotificationPermission = granted
    }

    LaunchedEffect(Unit) {
        val initiallyGranted = checkPermissions()
        viewModel.onPermissionsResult(initiallyGranted)
        hasNotificationPermission = checkNotificationPermission()
    }

    LaunchedEffect(feedbackMessage) {
        feedbackMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearFeedback()
        }
    }

    // 1. First-Launch Onboarding Carousel
    if (!isOnboardingCompleted) {
        OnboardingScreen(
            hasSmsPermission = hasPermissions,
            hasNotificationPermission = hasNotificationPermission,
            onRequestNotificationPermission = {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            },
            onRequestSmsPermissions = {
                permissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.READ_SMS,
                        Manifest.permission.SEND_SMS,
                        Manifest.permission.RECEIVE_SMS,
                        Manifest.permission.READ_CONTACTS
                    )
                )
            },
            onCompleteOnboarding = {
                viewModel.completeOnboarding()
            }
        )
    } else if (!hasPermissions) {
        // 2. Permission Gate Screen
        PermissionGateScreen(
            onRequestPermissions = {
                permissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.READ_SMS,
                        Manifest.permission.SEND_SMS,
                        Manifest.permission.RECEIVE_SMS,
                        Manifest.permission.READ_CONTACTS
                    )
                )
            }
        )
    } else {
        // 3. Main Application Scaffold
        val showBottomNav = currentScreen is ScreenDestination.Inbox ||
                currentScreen is ScreenDestination.Saved ||
                currentScreen is ScreenDestination.Settings ||
                currentScreen is ScreenDestination.Gateway

        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .background(PorcelainWhite),
            containerColor = PorcelainWhite,
            topBar = {
                if (showBottomNav) {
                    PulseMainTopBar(
                        title = when {
                            currentScreen is ScreenDestination.Gateway -> "Gateway Hub"
                            navTab == NavigationTab.INBOX -> "Pulse"
                            navTab == NavigationTab.SAVED -> "Saved"
                            else -> "Settings"
                        },
                        isLiveSyncing = true,
                        onSearchClick = {
                            viewModel.openSearch()
                        }
                    )
                }
            },
            bottomBar = {
                if (showBottomNav) {
                    PulseFloatingNavBar(
                        selectedTab = navTab,
                        unreadMessagesTotal = totalUnreadCount,
                        onTabSelected = { tab ->
                            viewModel.selectNavTab(tab)
                        },
                        onComposeClick = {
                            viewModel.openCompose()
                        }
                    )
                }
            },
            snackbarHost = {
                SnackbarHost(hostState = snackbarHostState)
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                when (val screen = currentScreen) {
                    is ScreenDestination.Inbox -> {
                        ConversationListScreen(
                            threads = conversations,
                            selectedCategory = category,
                            categoryCounts = categoryCounts,
                            onCategorySelected = { viewModel.selectCategory(it) },
                            onThreadClick = { thread ->
                                viewModel.selectThread(thread)
                            },
                            onTogglePin = { threadId, pinned ->
                                viewModel.togglePin(threadId, pinned)
                            },
                            onToggleArchive = { threadId, archived ->
                                viewModel.toggleArchive(threadId, archived)
                            }
                        )
                    }

                    is ScreenDestination.Search -> {
                        PulseSearchScreen(
                            searchQuery = searchQuery,
                            threads = allConversations,
                            onQueryChange = { viewModel.setSearchQuery(it) },
                            onCloseSearch = { viewModel.closeSearch() },
                            onThreadClick = { viewModel.selectThread(it) }
                        )
                    }

                    is ScreenDestination.ChatThread -> {
                        ChatThreadScreen(
                            threadId = screen.threadId,
                            address = screen.address,
                            displayName = screen.displayName,
                            messages = currentMessages,
                            isSending = isSending,
                            onBackClick = { viewModel.closeThread() },
                            onSendMessage = { text ->
                                viewModel.sendSms(screen.address, text, screen.threadId)
                            },
                            onToggleStar = { message ->
                                viewModel.toggleStar(message)
                            },
                            onScheduleMessage = { text, timestamp ->
                                viewModel.scheduleMessage(screen.address, screen.displayName, text, timestamp)
                            }
                        )
                    }

                    is ScreenDestination.Compose -> {
                        ComposeScreen(
                            contactSuggestions = contactSuggestions,
                            isSending = isSending,
                            onSearchContacts = { query -> viewModel.searchContacts(query) },
                            onBackClick = { viewModel.closeThread() },
                            onSendMessage = { recipient, text ->
                                viewModel.sendSms(recipient, text, null)
                                viewModel.openThreadDirect(recipient, "")
                            },
                            onSelectContactToChat = { number, name ->
                                viewModel.openThreadDirect(number, name)
                            }
                        )
                    }

                    is ScreenDestination.Saved -> {
                        SavedMessagesScreen(
                            starredMessages = starredMessages,
                            onOpenThread = { address ->
                                viewModel.openThreadDirect(address, "")
                            }
                        )
                    }

                    is ScreenDestination.Settings -> {
                        SettingsScreen(
                            hasSmsPermission = hasPermissions,
                            currentUser = currentUser,
                            businessAccount = businessAccount,
                            onSignInAndConvert = { activity, orgName ->
                                viewModel.signInAndConvertBusiness(activity, orgName)
                            },
                            onOpenGatewayConsole = {
                                viewModel.openGateway()
                            },
                            onSignOut = {
                                viewModel.signOutBusiness()
                            },
                            onPurgeExpiredOtps = {
                                viewModel.purgeExpiredOtps(24)
                            },
                            onExportBackup = {
                                viewModel.exportBackup(context)
                            },
                            blockedCount = blockedNumbers.size
                        )
                    }

                    is ScreenDestination.Gateway -> {
                        GatewayScreen(
                            businessAccount = businessAccount,
                            apiKeys = apiKeys,
                            dispatches = gatewayDispatches,
                            verificationResult = verificationResult,
                            onGenerateKey = { appName ->
                                viewModel.generateApiKey(appName)
                            },
                            onSimulateDispatch = { appName, phone, otp, key ->
                                viewModel.simulateGatewayDispatch(appName, phone, otp, key)
                            },
                            onVerifyOtp = { code, phone ->
                                viewModel.verifyOtp(code, phone)
                            },
                            onClearVerification = {
                                viewModel.clearVerificationResult()
                            }
                        )
                    }
                }
            }
        }
    }
}
