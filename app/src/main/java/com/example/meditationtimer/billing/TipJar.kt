package com.example.meditationtimer.billing

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ConsumeParams
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Voluntary tip jar via Google Play Billing. Three consumable products,
 * nothing in the app is gated behind them. Degrades quietly: when Play
 * or the products are unavailable (sideloads, F-Droid builds, the store
 * listing not yet live), [tips] simply stays empty.
 */
class TipJar(context: Context) : PurchasesUpdatedListener {

    data class Tip(val details: ProductDetails, val price: String)

    private val client = BillingClient.newBuilder(context.applicationContext)
        .setListener(this)
        .enablePendingPurchases(
            PendingPurchasesParams.newBuilder().enableOneTimeProducts().build()
        )
        .build()

    private val _tips = MutableStateFlow<List<Tip>>(emptyList())
    val tips: StateFlow<List<Tip>> = _tips

    private val _thanks = MutableStateFlow(false)
    val thanks: StateFlow<Boolean> = _thanks

    /** Connect and load the tip products; safe to call repeatedly. */
    fun open() {
        if (client.isReady) {
            queryProducts()
            return
        }
        client.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(result: BillingResult) {
                if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                    queryProducts()
                    consumeLeftovers()
                }
            }

            override fun onBillingServiceDisconnected() {}
        })
    }

    fun purchase(activity: Activity, tip: Tip) {
        val params = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(
                listOf(
                    BillingFlowParams.ProductDetailsParams.newBuilder()
                        .setProductDetails(tip.details)
                        .build()
                )
            )
            .build()
        client.launchBillingFlow(activity, params)
    }

    fun close() {
        client.endConnection()
    }

    override fun onPurchasesUpdated(result: BillingResult, purchases: MutableList<Purchase>?) {
        if (result.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            purchases.forEach { purchase ->
                if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) consume(purchase)
            }
        }
    }

    private fun queryProducts() {
        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(
                PRODUCT_IDS.map { id ->
                    QueryProductDetailsParams.Product.newBuilder()
                        .setProductId(id)
                        .setProductType(BillingClient.ProductType.INAPP)
                        .build()
                }
            )
            .build()
        client.queryProductDetailsAsync(params) { result, details ->
            if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                _tips.value = details
                    .sortedBy { it.oneTimePurchaseOfferDetails?.priceAmountMicros ?: 0 }
                    .map { Tip(it, it.oneTimePurchaseOfferDetails?.formattedPrice ?: "") }
            }
        }
    }

    /** A tip is consumed right away so it can be given again; also mops up
     *  any purchase whose consumption was interrupted. */
    private fun consume(purchase: Purchase) {
        val params = ConsumeParams.newBuilder().setPurchaseToken(purchase.purchaseToken).build()
        client.consumeAsync(params) { result, _ ->
            if (result.responseCode == BillingClient.BillingResponseCode.OK) _thanks.value = true
        }
    }

    private fun consumeLeftovers() {
        val params = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.INAPP)
            .build()
        client.queryPurchasesAsync(params) { result, purchases ->
            if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                purchases.forEach { purchase ->
                    if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) consume(purchase)
                }
            }
        }
    }

    private companion object {
        // Must match the in-app product ids configured in the Play Console.
        val PRODUCT_IDS = listOf("tip_small", "tip_coffee", "tip_generous")
    }
}
