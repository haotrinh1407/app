package com.example.ui

import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.R
import com.example.ui.screens.*
import com.example.ui.viewmodel.FarmViewModel
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

private object FarmRoutes {
    const val HOME = "home"
    const val MANAGE = "manage"
    const val KNOWLEDGE = "knowledge"
    const val ASSISTANT = "assistant"
    const val ACCOUNT = "account"
    const val ANIMALS = "animals"
    const val ANIMAL_DETAIL = "animal_detail/{animalId}"
    const val BARNS = "barns"
    const val REPRODUCTION = "reproduction"
    const val HEALTH = "health"
    const val FEED = "feed"
    const val TASKS = "tasks"
    const val FINANCE = "finance"
    const val ARTICLE_DETAIL = "article/{articleId}"
    const val NOTIFICATIONS = "notifications"
    const val ADMIN = "admin"

    fun animalDetail(animalId: String): String =
        "animal_detail/${URLEncoder.encode(animalId, StandardCharsets.UTF_8.toString())}"

    fun articleDetail(articleId: String): String =
        "article/${URLEncoder.encode(articleId, StandardCharsets.UTF_8.toString())}"
}

private data class PrimaryNavItem(
    val route: String,
    val labelRes: Int,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
)

private val PRIMARY_TABS = listOf(
    PrimaryNavItem(
        route = FarmRoutes.HOME,
        labelRes = R.string.nav_home,
        selectedIcon = Icons.Filled.Home,
        unselectedIcon = Icons.Outlined.Home,
        testTag = "nav_tab_home"
    ),
    PrimaryNavItem(
        route = FarmRoutes.MANAGE,
        labelRes = R.string.nav_manage,
        selectedIcon = Icons.Filled.GridView,
        unselectedIcon = Icons.Outlined.GridView,
        testTag = "nav_tab_manage"
    ),
    PrimaryNavItem(
        route = FarmRoutes.KNOWLEDGE,
        labelRes = R.string.nav_knowledge,
        selectedIcon = Icons.Filled.MenuBook,
        unselectedIcon = Icons.Outlined.MenuBook,
        testTag = "nav_tab_knowledge"
    ),
    PrimaryNavItem(
        route = FarmRoutes.ASSISTANT,
        labelRes = R.string.nav_assistant,
        selectedIcon = Icons.Filled.SmartToy,
        unselectedIcon = Icons.Outlined.SmartToy,
        testTag = "nav_tab_assistant"
    ),
    PrimaryNavItem(
        route = FarmRoutes.ACCOUNT,
        labelRes = R.string.nav_account,
        selectedIcon = Icons.Filled.Person,
        unselectedIcon = Icons.Outlined.Person,
        testTag = "nav_tab_account"
    )
)

@Composable
fun FarmCareApp(viewModel: FarmViewModel) {
    val isInitializing by viewModel.isInitializing.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(viewModel) {
        viewModel.snackbarMessage.collect { msg ->
            snackbarHostState.showSnackbar(
                message = msg,
                duration = SnackbarDuration.Short
            )
        }
    }

    if (isInitializing) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    CircularProgressIndicator()
                    Text(
                        text = stringResource(R.string.app_name),
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }
        return
    }

    if (currentUser == null) {
        Box(modifier = Modifier.fillMaxSize()) {
            AuthScreen(viewModel = viewModel)
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(16.dp)
            )
        }
        return
    }

    AuthenticatedFarmShell(
        viewModel = viewModel,
        snackbarHostState = snackbarHostState
    )
}

@Composable
private fun AuthenticatedFarmShell(
    viewModel: FarmViewModel,
    snackbarHostState: SnackbarHostState
) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val primaryRoutes = remember { PRIMARY_TABS.map { it.route }.toSet() }
    val showPrimaryNav = currentRoute in primaryRoutes

    val navigatePrimaryTab: (String) -> Unit = { route ->
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    val navigateRoute: (String) -> Unit = { target ->
        if (target in primaryRoutes) {
            navigatePrimaryTab(target)
        } else {
            navController.navigate(target) {
                launchSingleTop = true
            }
        }
    }

    val openAnimal: (String) -> Unit = { id ->
        navController.navigate(FarmRoutes.animalDetail(id))
    }

    val openArticle: (String) -> Unit = { id ->
        navController.navigate(FarmRoutes.articleDetail(id))
    }

    val openNotifications: () -> Unit = {
        navController.navigate(FarmRoutes.NOTIFICATIONS) {
            launchSingleTop = true
        }
    }

    val openAdmin: () -> Unit = {
        navController.navigate(FarmRoutes.ADMIN) {
            launchSingleTop = true
        }
    }

    val navContent: @Composable (Modifier) -> Unit = { modifier ->
        NavHost(
            navController = navController,
            startDestination = FarmRoutes.HOME,
            modifier = modifier
        ) {
            composable(FarmRoutes.HOME) {
                DashboardScreen(
                    viewModel = viewModel,
                    onNavigateRoute = navigateRoute,
                    onOpenAnimal = openAnimal,
                    onOpenArticle = openArticle,
                    onOpenNotifications = openNotifications
                )
            }
            composable(FarmRoutes.MANAGE) {
                ManagementHubScreen(
                    viewModel = viewModel,
                    onNavigateRoute = navigateRoute
                )
            }
            composable(FarmRoutes.KNOWLEDGE) {
                KnowledgeScreen(
                    viewModel = viewModel,
                    onOpenArticle = openArticle
                )
            }
            composable(FarmRoutes.ASSISTANT) {
                AssistantScreen(
                    viewModel = viewModel,
                    onOpenArticle = openArticle
                )
            }
            composable(FarmRoutes.ACCOUNT) {
                AccountScreen(
                    viewModel = viewModel,
                    onOpenAdmin = openAdmin,
                    onOpenNotifications = openNotifications
                )
            }
            composable(FarmRoutes.ANIMALS) {
                AnimalsScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() },
                    onOpenAnimal = openAnimal
                )
            }
            composable(
                route = FarmRoutes.ANIMAL_DETAIL,
                arguments = listOf(navArgument("animalId") { type = NavType.StringType })
            ) { entry ->
                val rawId = entry.arguments?.getString("animalId").orEmpty()
                val decodedId = remember(rawId) {
                    runCatching {
                        URLDecoder.decode(rawId, StandardCharsets.UTF_8.toString())
                    }.getOrDefault(Uri.decode(rawId))
                }
                AnimalDetailScreen(
                    animalId = decodedId,
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() }
                )
            }
            composable(FarmRoutes.BARNS) {
                BarnsScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() },
                    onOpenAnimal = openAnimal
                )
            }
            composable(FarmRoutes.REPRODUCTION) {
                ReproductionScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() }
                )
            }
            composable(FarmRoutes.HEALTH) {
                HealthScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() }
                )
            }
            composable(FarmRoutes.FEED) {
                FeedScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() }
                )
            }
            composable(FarmRoutes.TASKS) {
                TasksScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() }
                )
            }
            composable(FarmRoutes.FINANCE) {
                FinanceScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() }
                )
            }
            composable(
                route = FarmRoutes.ARTICLE_DETAIL,
                arguments = listOf(navArgument("articleId") { type = NavType.StringType })
            ) { entry ->
                val rawId = entry.arguments?.getString("articleId").orEmpty()
                val decodedId = remember(rawId) {
                    runCatching {
                        URLDecoder.decode(rawId, StandardCharsets.UTF_8.toString())
                    }.getOrDefault(Uri.decode(rawId))
                }
                ArticleDetailScreen(
                    articleId = decodedId,
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() },
                    onOpenArticle = openArticle
                )
            }
            composable(FarmRoutes.NOTIFICATIONS) {
                NotificationsScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() },
                    onNavigateRoute = navigateRoute
                )
            }
            composable(FarmRoutes.ADMIN) {
                AdminDashboardScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val useNavigationRail = maxWidth >= 600.dp
        if (useNavigationRail) {
            Row(modifier = Modifier.fillMaxSize()) {
                if (showPrimaryNav) {
                    NavigationRail(
                        containerColor = MaterialTheme.colorScheme.surface,
                        windowInsets = WindowInsets.safeDrawing.only(
                            WindowInsetsSides.Vertical + WindowInsetsSides.Start
                        )
                    ) {
                        Spacer(Modifier.height(12.dp))
                        PRIMARY_TABS.forEach { tab ->
                            val selected = currentRoute == tab.route
                            val label = stringResource(tab.labelRes)
                            NavigationRailItem(
                                selected = selected,
                                onClick = { navigatePrimaryTab(tab.route) },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                        contentDescription = label
                                    )
                                },
                                label = {
                                    Text(
                                        text = label,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                },
                                modifier = Modifier.testTag(tab.testTag)
                            )
                        }
                    }
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxSize()
                ) {
                    navContent(Modifier.fillMaxSize())
                    SnackbarHost(
                        hostState = snackbarHostState,
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .navigationBarsPadding()
                            .padding(16.dp)
                    )
                }
            }
        } else {
            Scaffold(
                contentWindowInsets = WindowInsets(0, 0, 0, 0),
                snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
                bottomBar = {
                    if (showPrimaryNav) {
                        NavigationBar(
                            containerColor = MaterialTheme.colorScheme.surface,
                            tonalElevation = 3.dp
                        ) {
                            PRIMARY_TABS.forEach { tab ->
                                val selected = currentRoute == tab.route
                                val label = stringResource(tab.labelRes)
                                NavigationBarItem(
                                    selected = selected,
                                    onClick = { navigatePrimaryTab(tab.route) },
                                    icon = {
                                        Icon(
                                            imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                            contentDescription = label
                                        )
                                    },
                                    label = {
                                        Text(
                                            text = label,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    },
                                    modifier = Modifier.testTag(tab.testTag)
                                )
                            }
                        }
                    }
                }
            ) { innerPadding ->
                val contentModifier = if (showPrimaryNav) {
                    Modifier
                        .fillMaxSize()
                        .padding(bottom = innerPadding.calculateBottomPadding())
                        .consumeWindowInsets(innerPadding)
                } else {
                    Modifier.fillMaxSize()
                }
                navContent(contentModifier)
            }
        }
    }
}
