package com.example.appfirebase

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.appfirebase.pages.HomePage
import com.example.appfirebase.pages.LoginPage
import com.example.appfirebase.pages.ObraScreen
import com.example.appfirebase.pages.SignupPage
import com.google.firebase.auth.FirebaseAuth

@Composable
fun MyAppNavigation(modifier: Modifier = Modifier, authViewModel: AuthViewModel) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "login") {
        composable("login") { LoginPage(modifier, navController, authViewModel) }
        composable("signup") { SignupPage(modifier, navController, authViewModel) }
        composable("home") { HomePage(modifier, navController, authViewModel) }
        composable("obras") {
            val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
            ObraScreen(modifier, userId)
        }
    }
}