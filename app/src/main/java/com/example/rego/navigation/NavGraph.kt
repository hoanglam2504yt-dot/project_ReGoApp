package com.example.rego.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.rego.data.local.UserSession
import com.example.rego.data.local.dao.*
import com.example.rego.data.repository.ProductRepository
import com.example.rego.ui.screens.*
import com.example.rego.ui.viewmodel.*
import kotlinx.coroutines.launch

@Composable
fun NavGraph(
    navController: NavHostController,
    userSession: UserSession,
    repository: ProductRepository,
    productDao: ProductDao,
    cartDao: CartDao,
    userDao: UserDao,
    orderDao: OrderDao,
    addressDao: AddressDao,
    isLoggedIn: Boolean,
    userId: Int?
) {
    val scope = rememberCoroutineScope()

    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(Screen.Home.route) {
            val homeViewModel: HomeViewModel = viewModel(factory = HomeViewModelFactory(repository))
            val cartViewModel: CartViewModel = viewModel(factory = CartViewModelFactory(cartDao, userDao, repository))
            val profileViewModel: ProfileViewModel = viewModel(factory = ProfileViewModelFactory(repository, userDao, orderDao))

            HomeScreen(
                viewModel = homeViewModel,
                cartViewModel = cartViewModel,
                profileViewModel = profileViewModel,
                isLoggedIn = isLoggedIn,
                userId = userId ?: 0,
                onProductClick = { id -> navController.navigate(Screen.ProductDetail.createRoute(id)) },
                onSeeAllClick = { navController.navigate(Screen.Categories.route) },
                onNotificationClick = { navController.navigate(Screen.Notifications.route) },
                onCartClick = { navController.navigate(Screen.Cart.route) },
                onProfileClick = { navController.navigate(Screen.Profile.route) },
                onLoginClick = { navController.navigate(Screen.Login.route) }
            )
        }

        composable(Screen.Products.route) {
            val homeViewModel: HomeViewModel = viewModel(factory = HomeViewModelFactory(repository))
            val cartViewModel: CartViewModel = viewModel(factory = CartViewModelFactory(cartDao, userDao, repository))
            val profileViewModel: ProfileViewModel = viewModel(factory = ProfileViewModelFactory(repository, userDao, orderDao))

            ProductListScreen(
                viewModel = homeViewModel,
                cartViewModel = cartViewModel,
                profileViewModel = profileViewModel,
                isLoggedIn = isLoggedIn,
                userId = userId ?: 0,
                onBackClick = { navController.popBackStack() },
                onProductClick = { id -> navController.navigate(Screen.ProductDetail.createRoute(id)) },
                onLoginClick = { navController.navigate(Screen.Login.route) }
            )
        }

        composable(
            route = Screen.Sell.routePattern,
            arguments = listOf(navArgument("productId") { type = NavType.StringType; nullable = true })
        ) { backStackEntry ->
            if (!isLoggedIn) {
                LaunchedEffect(Unit) { navController.navigate(Screen.Login.route) }
            } else {
                val productIdStr = backStackEntry.arguments?.getString("productId")
                val sellViewModel: SellViewModel = hiltViewModel<SellViewModel>()
                SellScreen(
                    viewModel = sellViewModel,
                    userId = userId ?: 0,
                    productId = productIdStr?.toIntOrNull(),
                    onBackClick = { navController.popBackStack() }
                )
            }
        }

        composable(Screen.Chat.route) {
            if (!isLoggedIn) {
                LaunchedEffect(Unit) { navController.navigate(Screen.Login.route) }
            } else {
                ChatScreen(
                    currentUserId = userId?.toString() ?: "",
                    onBackClick = { navController.popBackStack() },
                    onNotificationClick = { navController.navigate(Screen.Notifications.route) },
                    onChatClick = { otherUserId -> navController.navigate(Screen.MessageDetail.createRoute(otherUserId)) }
                )
            }
        }

        composable(
            route = Screen.MessageDetail.routePattern,
            arguments = listOf(navArgument("chatId") { type = NavType.StringType })
        ) { backStackEntry ->
            val chatId = backStackEntry.arguments?.getString("chatId") ?: ""
            val chatViewModel: ChatViewModel = hiltViewModel<ChatViewModel>()
            MessageDetailScreen(
                chatId = chatId,
                viewModel = chatViewModel,
                currentUserId = userId?.toString() ?: "",
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Screen.Profile.route) {
            if (!isLoggedIn) {
                LaunchedEffect(Unit) { navController.navigate(Screen.Login.route) }
            } else {
                val profileViewModel: ProfileViewModel = viewModel(factory = ProfileViewModelFactory(repository, userDao, orderDao))
                val authViewModel: AuthViewModel = viewModel(factory = AuthViewModelFactory(userDao, userSession))
                ProfileScreen(
                    viewModel = profileViewModel,
                    authViewModel = authViewModel,
                    userId = userId ?: 0,
                    isMine = true,
                    onNotificationClick = { navController.navigate(Screen.Notifications.route) },
                    onSettingsClick = { navController.navigate(Screen.Settings.route) },
                    onProductClick = { id -> navController.navigate(Screen.ProductDetail.createRoute(id)) },
                    onBackClick = { navController.popBackStack() },
                    onLogout = { navController.navigate(Screen.Login.route) { popUpTo(0) } },
                    onEditProfileClick = { navController.navigate(Screen.EditProfile.route) },
                    onLogoutClick = { scope.launch { authViewModel.logout() } }
                )
            }
        }

        composable(
            route = Screen.OtherProfile.routePattern,
            arguments = listOf(navArgument("userId") { type = NavType.IntType })
        ) { backStackEntry ->
            val otherId = backStackEntry.arguments?.getInt("userId") ?: 0
            val profileViewModel: ProfileViewModel = viewModel(factory = ProfileViewModelFactory(repository, userDao, orderDao))
            val authViewModel: AuthViewModel = viewModel(factory = AuthViewModelFactory(userDao, userSession))
            ProfileScreen(
                viewModel = profileViewModel,
                authViewModel = authViewModel,
                userId = otherId,
                isMine = false,
                onProductClick = { id -> navController.navigate(Screen.ProductDetail.createRoute(id)) },
                onBackClick = { navController.popBackStack() },
                onEditProfileClick = {},
                onLogoutClick = { scope.launch {} }
            )
        }

        composable(Screen.Settings.route) {
            val authViewModel: AuthViewModel = viewModel(factory = AuthViewModelFactory(userDao, userSession))
            SettingsScreen(
                onLogout = {
                    scope.launch {
                        authViewModel.logout()
                        navController.navigate(Screen.Login.route) { popUpTo(0) }
                    }
                },
                onNavigateBack = { navController.popBackStack() },
                onNotificationClick = { navController.navigate(Screen.Notifications.route) },
                onEditProfileClick = { navController.navigate(Screen.EditProfile.route) },
                onChangePasswordClick = { navController.navigate(Screen.ChangePassword.route) },
                onMyAddressesClick = { navController.navigate(Screen.MyAddresses.createRoute()) }
            )
        }

        composable(Screen.EditProfile.route) {
            val profileViewModel: ProfileViewModel = viewModel(factory = ProfileViewModelFactory(repository, userDao, orderDao))
            EditProfileScreen(
                viewModel = profileViewModel,
                userId = userId ?: 0,
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Screen.ChangePassword.route) {
            val authViewModel: AuthViewModel = viewModel(factory = AuthViewModelFactory(userDao, userSession))
            ChangePasswordScreen(
                viewModel = authViewModel,
                userId = userId ?: 0,
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.MyAddresses.routePattern,
            arguments = listOf(navArgument("isSelectionMode") { type = NavType.BoolType; defaultValue = false })
        ) { backStackEntry ->
            val isSelectionMode = backStackEntry.arguments?.getBoolean("isSelectionMode") ?: false
            val addressViewModel: AddressViewModel = viewModel(factory = AddressViewModelFactory(addressDao))
            AddressScreen(
                viewModel = addressViewModel,
                userId = userId ?: 0,
                isSelectionMode = isSelectionMode,
                onBackClick = { navController.popBackStack() },
                onAddressSelected = { if (isSelectionMode) navController.popBackStack() }
            )
        }

        composable(Screen.Notifications.route) {
            NotificationScreen(onBackClick = { navController.popBackStack() })
        }

        composable(Screen.Categories.route) {
            CategoriesScreen(onBackClick = { navController.popBackStack() })
        }

        composable(
            route = Screen.ProductDetail.routePattern,
            arguments = listOf(navArgument("productId") { type = NavType.StringType })
        ) { backStackEntry ->
            val productId = backStackEntry.arguments?.getString("productId") ?: ""
            val productDetailViewModel: ProductDetailViewModel = viewModel(
                factory = ProductDetailViewModelFactory(productDao, cartDao, userDao, repository)
            )
            ProductDetailScreen(
                productId = productId,
                viewModel = productDetailViewModel,
                currentUserId = userId ?: 0,
                onBackClick = { navController.popBackStack() },
                onEditClick = { id -> navController.navigate(Screen.Sell.createRoute(id.toIntOrNull())) },
                onSellerClick = { sellerId -> navController.navigate(Screen.OtherProfile.createRoute(sellerId)) },
                onBuyNowClick = { id -> navController.navigate(Screen.Checkout.createRoute(id)) },
                onChatClick = { sellerId -> navController.navigate(Screen.MessageDetail.createRoute(sellerId)) }
            )
        }

        composable(Screen.Cart.route) {
            val cartViewModel: CartViewModel = viewModel(factory = CartViewModelFactory(cartDao, userDao, repository))
            CartScreen(
                viewModel = cartViewModel,
                onBackClick = { navController.popBackStack() },
                onCheckout = { navController.navigate(Screen.Checkout.createRoute()) }
            )
        }

        composable(
            route = Screen.Checkout.routePattern,
            arguments = listOf(navArgument("productId") { type = NavType.StringType; nullable = true })
        ) { backStackEntry ->
            val productIdStr = backStackEntry.arguments?.getString("productId")
            val checkoutViewModel: CheckoutViewModel = viewModel(
                factory = CheckoutViewModelFactory(userDao, productDao, cartDao, orderDao, addressDao)
            )
            
            LaunchedEffect(productIdStr) {
                checkoutViewModel.loadCheckoutData(userId ?: 0, productIdStr?.toIntOrNull())
            }

            CheckoutScreen(
                viewModel = checkoutViewModel,
                onBackClick = { navController.popBackStack() },
                onPaymentSuccess = {
                    checkoutViewModel.placeOrder(userId ?: 0) {
                        navController.navigate(Screen.Home.route) { popUpTo(0) }
                    }
                },
                onChangeAddressClick = {
                    navController.navigate(Screen.MyAddresses.createRoute(isSelectionMode = true))
                }
            )
        }

        composable(Screen.Login.route) {
            val authViewModel: AuthViewModel = viewModel(factory = AuthViewModelFactory(userDao, userSession))
            LoginScreen(
                viewModel = authViewModel,
                onLoginSuccess = { navController.navigate(Screen.Home.route) { popUpTo(Screen.Login.route) { inclusive = true } } },
                onNavigateToRegister = { navController.navigate(Screen.Register.route) },
                onRegisterClick = { navController.navigate(Screen.Register.route) }
            )
        }

        composable(Screen.Register.route) {
            val authViewModel: AuthViewModel = viewModel(factory = AuthViewModelFactory(userDao, userSession))
            RegisterScreen(
                viewModel = authViewModel,
                onRegisterSuccess = { navController.popBackStack() },
                onLoginClick = { navController.popBackStack() },
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}
