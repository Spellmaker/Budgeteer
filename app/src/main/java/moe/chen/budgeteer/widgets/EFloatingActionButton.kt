package moe.chen.budgeteer.widgets

import androidx.annotation.StringRes
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource

@Composable
fun EFloatingActionButton(
    enabled: Boolean,
    onClick: () -> Unit,
    icon: ImageVector,
    @StringRes contentDescription: Int,
) {
    FloatingActionButton(
        onClick = {
            if (enabled) {
                onClick()
            }
        }, modifier = if (!enabled) Modifier.alpha(0.5f) else Modifier
    ) {
        Icon(
            imageVector = icon,
            contentDescription = stringResource(id = contentDescription)
        )
    }
}