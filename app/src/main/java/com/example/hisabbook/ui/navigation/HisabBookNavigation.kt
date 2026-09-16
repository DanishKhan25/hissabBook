package com.example.hisabbook.ui.navigation

import android.app.Application
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Assignment
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Inventory
import androidx.compose.material.icons.rounded.People
import androidx.compose.material.icons.rounded.Receipt
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.example.hisabbook.R
import com.example.hisabbook.ui.customer.CustomerDetailScreen
import com.example.hisabbook.ui.customer.CustomerListScreen
import com.example.hisabbook.ui.dashboard.DashboardScreen
import com.example.hisabbook.ui.inventory.InventoryListScreen
import com.example.hisabbook.ui.invoice.CreateInvoiceScreen
import com.example.hisabbook.ui.invoice.InvoiceDetailScreen
import com.example.hisabbook.ui.invoice.InvoiceListScreen
import com.example.hisabbook.ui.onboarding.OnboardingScreen
import com.example.hisabbook.ui.payment.PaymentHistoryScreen
import com.example.hisabbook.ui.quotation.CreateQuotationScreen
import com.example.hisabbook.ui.quotation.QuotationDetailScreen
import com.example.hisabbook.ui.quotation.QuotationListScreen
import com.example.hisabbook.ui.settings.SettingsScreen
import com.example.hisabbook.ui.viewmodel.CustomerViewModel
import com.example.hisabbook.ui.viewmodel.DashboardViewModel
import com.example.hisabbook.ui.viewmodel.HisabBookViewModelFactory
import com.example.hisabbook.ui.viewmodel.InventoryViewModel
import com.example.hisabbook.ui.viewmodel.InvoiceViewModel
import com.example.hisabbook.ui.viewmodel.PaymentViewModel
import com.example.hisabbook.ui.viewmodel.QuotationViewModel
import com.example.hisabbook.ui.viewmodel.SettingsViewModel
import com.example.hisabbook.util.LocaleHelper

@Composable
fun HisabBookApp(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val factory = HisabBookViewModelFactory(context.applicationContext as Application)

    val dashboardViewModel: DashboardViewModel = viewModel(factory = factory)
    val settingsViewModel: SettingsViewModel = viewModel(factory = factory)
    val customerViewModel: CustomerViewModel = viewModel(factory = factory)
    val paymentViewModel: PaymentViewModel = viewModel(factory = factory)
    val inventoryViewModel: InventoryViewModel = viewModel(factory = factory)
    val quotationViewModel: QuotationViewModel = viewModel(factory = factory)
    val invoiceViewModel: InvoiceViewModel = viewModel(factory = factory)

    val backStack: NavBackStack<NavKey> = rememberNavBackStack(HisabBookNavKey.HomeDashboard)
    val currentKey = backStack.lastOrNull() ?: HisabBookNavKey.HomeDashboard

    val businessProfile by dashboardViewModel.businessProfile.collectAsStateWithLifecycle()

    LaunchedEffect(businessProfile) {
        businessProfile?.let { prof ->
            if (prof.languagePreference.isNotBlank()) {
                LocaleHelper.applyLocale(context, prof.languagePreference)
            }
            if (!prof.isOnboardingCompleted && currentKey !is HisabBookNavKey.Onboarding) {
                backStack.clear()
                backStack.add(HisabBookNavKey.Onboarding)
            }
        }
    }

    val showBottomBar = currentKey is HisabBookNavKey.HomeDashboard ||
            currentKey is HisabBookNavKey.CustomerList ||
            currentKey is HisabBookNavKey.QuotationList ||
            currentKey is HisabBookNavKey.InvoiceList ||
            currentKey is HisabBookNavKey.InventoryList ||
            currentKey is HisabBookNavKey.PaymentHistory

    fun popBack() {
        if (backStack.size > 1) {
            backStack.removeAt(backStack.lastIndex)
        }
    }

    val navEntryProvider = entryProvider<NavKey> {
        entry<HisabBookNavKey.Onboarding> {
            OnboardingScreen(
                settingsViewModel = settingsViewModel,
                onOnboardingComplete = {
                    backStack.clear()
                    backStack.add(HisabBookNavKey.HomeDashboard)
                }
            )
        }

        entry<HisabBookNavKey.HomeDashboard> {
            DashboardScreen(
                dashboardViewModel = dashboardViewModel,
                customerViewModel = customerViewModel,
                paymentViewModel = paymentViewModel,
                inventoryViewModel = inventoryViewModel,
                onNavigateToSettings = {
                    backStack.add(HisabBookNavKey.Settings)
                },
                onCreateQuotationClick = {
                    backStack.add(HisabBookNavKey.CreateQuotation())
                },
                onCreateBillClick = {
                    backStack.add(HisabBookNavKey.CreateInvoice())
                },
                onLowStockClick = {
                    inventoryViewModel.setLowStockFilter(true)
                    backStack.clear()
                    backStack.add(HisabBookNavKey.InventoryList)
                },
                onInvoiceClick = { invoiceId ->
                    backStack.add(HisabBookNavKey.InvoiceDetail(invoiceId))
                }
            )
        }

        entry<HisabBookNavKey.Settings> {
            SettingsScreen(
                viewModel = settingsViewModel,
                onBackClick = { popBack() }
            )
        }

        entry<HisabBookNavKey.CustomerList> {
            CustomerListScreen(
                viewModel = customerViewModel,
                onCustomerClick = { customerId ->
                    backStack.add(HisabBookNavKey.CustomerDetail(customerId))
                }
            )
        }

        entry<HisabBookNavKey.CustomerDetail> { key ->
            CustomerDetailScreen(
                customerId = key.customerId,
                customerViewModel = customerViewModel,
                paymentViewModel = paymentViewModel,
                onBackClick = { popBack() },
                onNewQuotationClick = { custId ->
                    backStack.add(HisabBookNavKey.CreateQuotation(custId))
                },
                onNewBillClick = { custId ->
                    backStack.add(HisabBookNavKey.CreateInvoice(custId))
                }
            )
        }

        entry<HisabBookNavKey.QuotationList> {
            QuotationListScreen(
                viewModel = quotationViewModel,
                onQuotationClick = { quotationId ->
                    backStack.add(HisabBookNavKey.QuotationDetail(quotationId))
                },
                onCreateQuotationClick = {
                    backStack.add(HisabBookNavKey.CreateQuotation())
                }
            )
        }

        entry<HisabBookNavKey.CreateQuotation> { key ->
            CreateQuotationScreen(
                quotationViewModel = quotationViewModel,
                preSelectedCustomerId = key.customerId,
                quotationIdToEdit = key.quotationIdToEdit,
                onBackClick = { popBack() },
                onQuotationCreated = { quotationId ->
                    popBack()
                    backStack.add(HisabBookNavKey.QuotationDetail(quotationId))
                }
            )
        }

        entry<HisabBookNavKey.QuotationDetail> { key ->
            QuotationDetailScreen(
                quotationId = key.quotationId,
                quotationViewModel = quotationViewModel,
                settingsViewModel = settingsViewModel,
                onBackClick = { popBack() },
                onInvoiceCreated = { createdInvoiceId ->
                    popBack()
                    backStack.add(HisabBookNavKey.InvoiceDetail(createdInvoiceId))
                },
                onEditQuotationClick = { quotationId ->
                    backStack.add(HisabBookNavKey.CreateQuotation(quotationIdToEdit = quotationId))
                }
            )
        }

        entry<HisabBookNavKey.InvoiceList> {
            InvoiceListScreen(
                invoiceViewModel = invoiceViewModel,
                onInvoiceClick = { invoiceId ->
                    backStack.add(HisabBookNavKey.InvoiceDetail(invoiceId))
                },
                onCreateInvoiceClick = {
                    backStack.add(HisabBookNavKey.CreateInvoice())
                }
            )
        }

        entry<HisabBookNavKey.CreateInvoice> { key ->
            CreateInvoiceScreen(
                invoiceViewModel = invoiceViewModel,
                preSelectedCustomerId = key.customerId,
                preSelectedQuotationId = key.quotationId,
                invoiceIdToEdit = key.invoiceIdToEdit,
                onBackClick = { popBack() },
                onInvoiceCreated = { invoiceId ->
                    popBack()
                    backStack.add(HisabBookNavKey.InvoiceDetail(invoiceId))
                }
            )
        }

        entry<HisabBookNavKey.InvoiceDetail> { key ->
            InvoiceDetailScreen(
                invoiceId = key.invoiceId,
                invoiceViewModel = invoiceViewModel,
                paymentViewModel = paymentViewModel,
                customerViewModel = customerViewModel,
                settingsViewModel = settingsViewModel,
                onBackClick = { popBack() },
                onEditInvoiceClick = { invoiceId ->
                    backStack.add(HisabBookNavKey.CreateInvoice(invoiceIdToEdit = invoiceId))
                }
            )
        }

        entry<HisabBookNavKey.PaymentHistory> {
            PaymentHistoryScreen(
                paymentViewModel = paymentViewModel,
                customerViewModel = customerViewModel
            )
        }

        entry<HisabBookNavKey.InventoryList> {
            InventoryListScreen(
                viewModel = inventoryViewModel
            )
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    NavigationBarItem(
                        selected = currentKey is HisabBookNavKey.HomeDashboard,
                        onClick = {
                            if (currentKey !is HisabBookNavKey.HomeDashboard) {
                                backStack.clear()
                                backStack.add(HisabBookNavKey.HomeDashboard)
                            }
                        },
                        icon = { Icon(Icons.Rounded.Home, contentDescription = stringResource(R.string.nav_home)) },
                        label = { Text(stringResource(R.string.nav_home)) }
                    )

                    NavigationBarItem(
                        selected = currentKey is HisabBookNavKey.CustomerList,
                        onClick = {
                            if (currentKey !is HisabBookNavKey.CustomerList) {
                                backStack.clear()
                                backStack.add(HisabBookNavKey.CustomerList)
                            }
                        },
                        icon = { Icon(Icons.Rounded.People, contentDescription = stringResource(R.string.nav_customers)) },
                        label = { Text(stringResource(R.string.nav_customers)) }
                    )

                    NavigationBarItem(
                        selected = currentKey is HisabBookNavKey.QuotationList,
                        onClick = {
                            if (currentKey !is HisabBookNavKey.QuotationList) {
                                backStack.clear()
                                backStack.add(HisabBookNavKey.QuotationList)
                            }
                        },
                        icon = { Icon(Icons.AutoMirrored.Rounded.Assignment, contentDescription = stringResource(R.string.nav_quotations)) },
                        label = { Text(stringResource(R.string.nav_quotations)) }
                    )

                    NavigationBarItem(
                        selected = currentKey is HisabBookNavKey.InvoiceList,
                        onClick = {
                            if (currentKey !is HisabBookNavKey.InvoiceList) {
                                backStack.clear()
                                backStack.add(HisabBookNavKey.InvoiceList)
                            }
                        },
                        icon = { Icon(Icons.Rounded.Receipt, contentDescription = stringResource(R.string.nav_invoices)) },
                        label = { Text(stringResource(R.string.nav_invoices)) }
                    )

                    NavigationBarItem(
                        selected = currentKey is HisabBookNavKey.InventoryList,
                        onClick = {
                            if (currentKey !is HisabBookNavKey.InventoryList) {
                                backStack.clear()
                                backStack.add(HisabBookNavKey.InventoryList)
                            }
                        },
                        icon = { Icon(Icons.Rounded.Inventory, contentDescription = stringResource(R.string.nav_inventory)) },
                        label = { Text(stringResource(R.string.nav_inventory)) }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavDisplay(
            backStack = backStack,
            entryProvider = navEntryProvider,
            modifier = Modifier.padding(innerPadding)
        )
    }
}
