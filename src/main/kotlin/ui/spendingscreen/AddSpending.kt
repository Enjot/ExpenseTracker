package ui.spendingscreen

import Validator
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sqldelight.Category

@Composable
fun AddSpending(
    onAddClick: (String, Double, Category) -> Unit,
    onCloseDialog: () -> Unit,
    category: List<Category>
) {

    var name by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var categoryName by remember { mutableStateOf("Wybierz kategorię") }
    var chosenCategory by remember { mutableStateOf(0) }
    var isAmountValid by remember { mutableStateOf(true) }
    var isNameValid by remember { mutableStateOf(true) }

    Row {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .background(MaterialTheme.colorScheme.surface)
                .padding(48.dp)
                .fillMaxHeight()
                .wrapContentWidth()

        ) {
            Text(
                text = "Dodaj wydatek",
                style = MaterialTheme.typography.displaySmall
            )
            Spacer(modifier = Modifier.height(24.dp))
            OutlinedTextField(
                value = name,
                label = { Text("Nazwa") },
                onValueChange = { name = it },
                isError = !isNameValid,
                supportingText = {if (!isNameValid) {
                    Row{
                        Icon(
                            painterResource("drawable/icons/error.svg"),
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            "Nieprawidłowa nazwa",
                            fontSize = 16.sp
                        )
                    }
                } else null},
                modifier = Modifier
                    .padding(12.dp)
                    .focusable()
//                            .focusRequester(focusRequester)
            )
            Spacer(modifier = Modifier.height(36.dp))
            OutlinedTextField(
                value = categoryName,
                label = { Text("Kategria") },
                singleLine = true,
                readOnly = true,
                onValueChange = { },
                modifier = Modifier
                    .padding(12.dp)
                    .focusable()

//                            .focusRequester(focusRequester)
            )
            Spacer(modifier = Modifier.height(36.dp))
            OutlinedTextField(
                value = amount,
                label = { Text("Kwota") },
                onValueChange = { amount = it },
                isError = !isAmountValid,
                supportingText = {if (!isAmountValid) {
                    Row{
                        Icon(
                            painterResource("drawable/icons/error.svg"),
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            "Nieprawidłowa kwota",
                            fontSize = 16.sp
                        )
                    }
                } else null},
                modifier = Modifier
                    .padding(12.dp)
                    .focusable()
//                            .focusRequester(focusRequester)
            )
            Spacer(modifier = Modifier.height(36.dp))
            Button(
                onClick = {
                    when {
                        Validator.isValidAmount(amount) -> isAmountValid = true
                        !Validator.isValidAmount(amount) -> isAmountValid = false
                    }
                    when {
                        Validator.isEmptyString(name) -> isNameValid = true
                        !Validator.isEmptyString(name) -> isNameValid = false
                    }
                    if (isNameValid && isAmountValid) {
                        onAddClick(name, amount.toDouble(), category[chosenCategory])
                        run { onCloseDialog() }
                    }
                },
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("Dodaj")
            }
        }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .background(MaterialTheme.colorScheme.surface)
                .padding(48.dp)
                .fillMaxHeight()
                .weight(1f)
        ) {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(200.dp)
            ) {
                items(category) {
                    Text(
                        text = it.name,
                        modifier = Modifier
                            .padding(4.dp)
                            .clickable {
                                chosenCategory = category.indexOf(it)
                                categoryName = it.name
                            }
                    )
                }
            }
        }
    }
}