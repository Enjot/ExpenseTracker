package ui.spendingscreen

import data.ViewModels.SpendingScreenModel
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.launch
import ui.limitscreen.monthNames
import ui.utils.AddButton
import ui.utils.ScreenContent
import ui.utils.Scrollbar


@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class, ExperimentalFoundationApi::class)
@Composable
fun SpendingScreen(
    model: SpendingScreenModel
) {
    val spendings = model.spendings.collectAsState(emptyList())
    val categories = model.categories.collectAsState(emptyList())
    val stateVertical = rememberLazyGridState()
    var dialog by remember { mutableStateOf(false) }
    var showScrollbar by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    var showBottomSheet by remember { mutableStateOf(false) }
    var editingSpending by remember { mutableStateOf(false) }

    ScreenContent(
        modifier = Modifier
            .onPointerEvent(PointerEventType.Enter) { showScrollbar = true }
            .onPointerEvent(PointerEventType.Exit) { showScrollbar = false }

    ) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                Text(
                    text = "Codzienne wydatki",
                    style = MaterialTheme.typography.displayLarge,
                    modifier = Modifier.weight(1f)
                )
                // Had to wrap it in Box for normal dropdown menu behaviour
                Box(
                    modifier = Modifier
                        .padding(24.dp)
                ) {
                    SortPicker(
                        onSortClick = { type -> model.setSortType(type) },
                        value = model.sortType.value.visibleName,
                    )
                }
                OutlinedButton(
                    onClick = { showBottomSheet = !showBottomSheet },
                    modifier = Modifier
                        .wrapContentSize(Alignment.BottomEnd)
                        .padding(top = 24.dp, bottom = 24.dp, end = 36.dp)
                ) {
                    Text("Filtruj")
                    Spacer(modifier = Modifier.width(24.dp))
                    Icon(
                        painterResource("drawable/icons/filter.svg"),
                        contentDescription = null
                    )
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
            LazyVerticalGrid(
                columns = GridCells.Adaptive(300.dp),
                state = stateVertical,
            ) {
                items(spendings.value, key = { it.id }) { spending ->
                    SingleSpendingItem(
                        { id, name, amount ->
                            model.edit(
                                id,
                                name,
                                amount

                            )
                        },
                        { id ->
                            model.delete(id)
                        },
                        spending,
                        modifier = Modifier
                            .animateItemPlacement()

                    )
                }
                item {
                    Spacer(modifier = Modifier.height(128.dp))
                }
            }
        }
        AddButton(
            text = "Dodaj wydatek",
            expanded = !stateVertical.canScrollBackward || !stateVertical.canScrollForward,
            onClick = { dialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(48.dp)
        )
        Scrollbar(
            showScrollbar,
            rememberScrollbarAdapter(stateVertical),
            modifier = Modifier.align(Alignment.CenterEnd)
        )
    }
    if (dialog) {
        AlertDialog(
            onDismissRequest = { dialog = false },
            properties = DialogProperties(
                usePlatformDefaultWidth = false
            ),
            modifier = Modifier
                .width(1000.dp)
                .padding(vertical = 24.dp)
                .clip(RoundedCornerShape(24.dp))
        ) {
            AddSpending(
                { name, amount, category ->
                    model.insert(
                        name,
                        amount,
                        category
                    )
                },
                { dialog = !dialog },
                categories.value
            )
        }
    }
    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = {
                showBottomSheet = false
            },
            sheetState = sheetState
        ) {

            var selectedMonth by remember { mutableStateOf(model.filterByDate.value.selectedMonth) }
            var selectedYear by remember { mutableStateOf(model.filterByDate.value.selectedYear) }
            var isFiltered by remember { mutableStateOf(model.filterByDate.value.isFiltered) }

            Column {
                DateFilterSelector(
                    monthsNames = monthNames,
                    selectedMonth = selectedMonth,
                    selectedYear = selectedYear,
                    onYearSelect = { selectedYear = it.toInt() },
                    onMonthSelect = { selectedMonth = it.toInt() },
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Text("Włącz filtrowanie po dacie")
                    Spacer(modifier = Modifier.width(24.dp))
                    Switch(
                        checked = isFiltered,
                        onCheckedChange = { isFiltered = !isFiltered },
                    )
                }
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(200.dp),
                    modifier = Modifier.padding(24.dp)
                ) {
                    item {
                        Text(
                            text = "Pokaż wszystkie",
                            modifier = Modifier
                                .padding(4.dp)
                                .clickable { model.setFilterByCategory("") }
                        )
                    }
                    items(categories.value) { category ->
                        Text(
                            text = category.name,
                            modifier = Modifier
                                .padding(4.dp)
                                .clickable {
                                    model.setFilterByCategory(category.name)
                                }
                        )
                    }
                }
                Row {
                    TextButton(
                        onClick = {
                            model.setFilterByCategory("")
                            model.resetDateFilter()
                            scope.launch { sheetState.hide() }.invokeOnCompletion {
                                if (!sheetState.isVisible) {
                                    showBottomSheet = false
                                }
                            }
                        },
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Text("Wyczyść filtrowanie")
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    Button(
                        onClick = {
                            model.setFilterByDate(selectedMonth, selectedYear, isFiltered)
                            scope.launch { sheetState.hide() }.invokeOnCompletion {
                                if (!sheetState.isVisible) showBottomSheet = false
                            }

                        },
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Text("Filtruj")
                    }
                }
            }
        }
    }
}