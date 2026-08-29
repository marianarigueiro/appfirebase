package com.example.appfirebase.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ImageNotSupported
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.appfirebase.ui.theme.ArranhaCeuColor
import com.example.appfirebase.ui.theme.BarragemColor
import com.example.appfirebase.ui.theme.PonteColor
import com.example.appfirebase.ui.theme.PrimaryColor
import com.google.firebase.firestore.FirebaseFirestore

data class ObraEngenharia(
    val id: String = "",
    val titulo: String = "",
    val tipo: String = "",          // "Ponte Estaiada", "Barragem" ou "Arranha-céu"
    val localizacao: String = "",
    val descricao: String = "",
    val imagemUrl: String = "",
    val userId: String = ""
)

val tiposDeObra = listOf("Ponte Estaiada", "Barragem", "Arranha-céu")

fun corPorTipo(tipo: String): Color = when (tipo) {
    "Ponte Estaiada" -> PonteColor
    "Barragem" -> BarragemColor
    else -> ArranhaCeuColor
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ObraScreen(modifier: Modifier = Modifier, userId: String) {
    val db = FirebaseFirestore.getInstance()

    var titulo by remember { mutableStateOf("") }
    var tipo by remember { mutableStateOf(tiposDeObra[0]) }
    var localizacao by remember { mutableStateOf("") }
    var descricao by remember { mutableStateOf("") }
    var imagemUrl by remember { mutableStateOf("") }
    var editandoId by remember { mutableStateOf<String?>(null) }
    var expandedTipo by remember { mutableStateOf(false) }

    val obras = remember { mutableStateListOf<ObraEngenharia>() }

    // READ em tempo real, só as obras cadastradas pelo usuário logado
    LaunchedEffect(Unit) {
        db.collection("obras")
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, _ ->
                obras.clear()
                snapshot?.documents?.forEach { doc ->
                    obras.add(
                        ObraEngenharia(
                            id = doc.id,
                            titulo = doc.getString("titulo") ?: "",
                            tipo = doc.getString("tipo") ?: "",
                            localizacao = doc.getString("localizacao") ?: "",
                            descricao = doc.getString("descricao") ?: "",
                            imagemUrl = doc.getString("imagemUrl") ?: ""
                        )
                    )
                }
            }
    }

    LazyColumn(
        modifier = modifier.fillMaxSize().background(Color(0xFFECEFF1)).padding(16.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        if (editandoId == null) "Nova Obra" else "Editando Obra",
                        fontSize = 18.sp, fontWeight = FontWeight.Bold, color = PrimaryColor
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = titulo, onValueChange = { titulo = it },
                        label = { Text("Nome da obra") }, singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    ExposedDropdownMenuBox(expanded = expandedTipo, onExpandedChange = { expandedTipo = it }) {
                        OutlinedTextField(
                            value = tipo, onValueChange = {}, readOnly = true,
                            label = { Text("Tipo") }, modifier = Modifier.fillMaxWidth().menuAnchor()
                        )
                        ExposedDropdownMenu(expanded = expandedTipo, onDismissRequest = { expandedTipo = false }) {
                            tiposDeObra.forEach { opcao ->
                                DropdownMenuItem(text = { Text(opcao) }, onClick = {
                                    tipo = opcao; expandedTipo = false
                                })
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = localizacao, onValueChange = { localizacao = it },
                        label = { Text("Localização") }, singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = descricao, onValueChange = { descricao = it },
                        label = { Text("Descrição") },
                        minLines = 2,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = imagemUrl, onValueChange = { imagemUrl = it },
                        label = { Text("URL da imagem") }, singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            val dados = mapOf(
                                "titulo" to titulo, "tipo" to tipo, "localizacao" to localizacao,
                                "descricao" to descricao, "imagemUrl" to imagemUrl, "userId" to userId
                            )
                            if (editandoId == null) {
                                db.collection("obras").add(dados)                          // CREATE
                            } else {
                                db.collection("obras").document(editandoId!!).set(dados)   // UPDATE
                            }
                            titulo = ""; localizacao = ""; descricao = ""; imagemUrl = ""; editandoId = null
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor),
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (editandoId == null) "Salvar Obra" else "Atualizar Obra")
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            Text("Obras cadastradas", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = PrimaryColor)
            Spacer(modifier = Modifier.height(8.dp))

            if (obras.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.ImageNotSupported, contentDescription = null, tint = Color.Gray)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Nenhuma obra cadastrada ainda", color = Color.Gray)
                }
            }
        }

        items(obras) { obra ->
            Card(
                shape = RoundedCornerShape(14.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)
            ) {
                Column {
                    if (obra.imagemUrl.isNotBlank()) {
                        AsyncImage(
                            model = obra.imagemUrl,
                            contentDescription = obra.titulo,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp)
                                .clip(RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp))
                        )
                    }
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50))
                                    .background(corPorTipo(obra.tipo))
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(obra.tipo, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(obra.titulo, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Text(obra.localizacao, fontSize = 13.sp, color = Color.Gray)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(obra.descricao, fontSize = 14.sp)

                        Row(
                            horizontalArrangement = Arrangement.End,
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                        ) {
                            IconButton(onClick = {
                                titulo = obra.titulo; tipo = obra.tipo
                                localizacao = obra.localizacao; descricao = obra.descricao
                                imagemUrl = obra.imagemUrl; editandoId = obra.id
                            }) { Icon(Icons.Default.Edit, contentDescription = "Editar", tint = PrimaryColor) }

                            IconButton(onClick = {
                                db.collection("obras").document(obra.id).delete()  // DELETE
                            }) { Icon(Icons.Default.Delete, contentDescription = "Excluir", tint = Color(0xFFD32F2F)) }
                        }
                    }
                }
            }
        }
    }
}