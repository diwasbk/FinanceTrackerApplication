package com.example.financetrackerapplication.utils

import android.view.View
import android.widget.LinearLayout
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView

class LoadingUtils {

    companion object {
        // Show loading layout with animation and hide RecyclerView
        fun showLoading(loadingLayout: LinearLayout, animationView: LottieAnimationView, recyclerView: RecyclerView) {
            loadingLayout.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
            if (!animationView.isAnimating) {
                animationView.playAnimation() // Start animation
            }
        }

        // Hide loading layout, stop animation, and show RecyclerView
        fun hideLoading(loadingLayout: LinearLayout, animationView: LottieAnimationView, recyclerView: RecyclerView) {
            loadingLayout.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
            animationView.cancelAnimation() // Stop animation
        }
    }
}