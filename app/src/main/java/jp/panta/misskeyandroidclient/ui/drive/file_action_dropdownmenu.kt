package jp.panta.misskeyandroidclient.ui.drive

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import jp.panta.misskeyandroidclient.R
import jp.panta.misskeyandroidclient.model.drive.FileProperty

@Composable
fun FileActionDropdownMenu(
    property: FileProperty,
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    onNsfwMenuItemClicked: () -> Unit,
    onUpdateNameMenuItemClicked: ()-> Unit,
    onDeleteMenuItemClicked: ()-> Unit,
) {


    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismissRequest,
        modifier = Modifier.wrapContentWidth(),
    ) {
        DropdownMenuItem(
            onClick = onNsfwMenuItemClicked
        ) {
            if(property.isSensitive) {
                Icon(
                    painter = painterResource(
                        id = R.drawable.ic_baseline_image_24),
                    contentDescription = "nsfwを解除",
                    modifier = Modifier.size(24.dp)

                )
                Text(text = "nsfwを解除")
            }else{
                Icon(
                    painter = painterResource(
                        id = R.drawable.ic_baseline_hide_image_24),
                    contentDescription = "nsfwにする",
                    modifier = Modifier.size(24.dp)
                )
                Text(text = "nsfwにする")
            }
        }
        DropdownMenuItem(
            onClick = onUpdateNameMenuItemClicked
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_edit_black_24dp),
                modifier = Modifier.size(24.dp),
                contentDescription = stringResource(R.string.rename)
            )
            Text(text = stringResource(R.string.rename))
        }
        Divider()
        DropdownMenuItem(
            onClick = onDeleteMenuItemClicked,
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_delete_black_24dp),
                modifier = Modifier.size(24.dp),
                contentDescription = stringResource(R.string.delete)
            )
            Text(text = stringResource(R.string.delete))
        }
    }


}

@Composable
fun ConfirmDeleteFilePropertyDialog(
    filename: String,
    onDismissRequest: () -> Unit,
    onConfirmed: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,

        title = {
            Text("ファイル削除の確認")
        },
        confirmButton = {
            TextButton(onClick = onConfirmed) {
                Text("削除")
            }
        },
        text = {
            Text("${filename}を削除しますか？")
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text("やめる")
            }
        }
    )
}