package com.junkfood.seal.ui.component.download

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.junkfood.seal.R

@Composable
fun ConsoleOutputCard(
    modifier: Modifier = Modifier,
    downloadLog: String,
) {
    Column(
        modifier = modifier,
    ) {
        Text(
            text = stringResource(id = R.string.logs),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .padding(4.dp)
                .padding(bottom = 12.dp),
            fontFamily = FontFamily.Monospace
        )
        //split the string into lines and create a list of items
        val lines = downloadLog.split("\n")
        OutlinedCard(
            modifier = Modifier.fillMaxSize(),
            shape = MaterialTheme.shapes.small
        ) {
            LazyColumn(
                Modifier
                    .fillMaxSize()
                    .padding(6.dp)
            ) {
                items(lines) {
                    Text(
                        text = it,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(vertical = 5.dp),
                        fontSize = MaterialTheme.typography.bodySmall.fontSize
                    )
                }
            }
        }
    }
}