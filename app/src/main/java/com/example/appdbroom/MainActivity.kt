package com.example.appdbroom

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.room.Room
import com.example.appdbroom.roomDB.Pessoa
import com.example.appdbroom.roomDB.PessoaDataBase
import com.example.appdbroom.viewModel.PessoaViewModel
import com.example.appdbroom.viewModel.Repository
import kotlinx.coroutines.delay
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {

    private val db by lazy {
        Room.databaseBuilder(
            applicationContext,
            PessoaDataBase::class.java,
            "pessoa.db"
        ).build()
    }

    private val viewModel by viewModels<PessoaViewModel> {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return PessoaViewModel(Repository(db)) as T
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            App(viewModel, this)
        }
    }
}




@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App(viewModel: PessoaViewModel, mainActivity: MainActivity) {
    var nome by remember { mutableStateOf("") }
    var telefone by remember { mutableStateOf("") }
    var pessoaList by remember { mutableStateOf(listOf<Pessoa>()) }
    var showSuccessMessage by remember { mutableStateOf(false) }

    // Atualiza a lista de pessoas quando o ViewModel muda
    LaunchedEffect(viewModel) {
        viewModel.getPessoa().observe(mainActivity) {
            pessoaList = it
        }
    }



    // Controla o temporizador de exibição da mensagem de sucesso
    if (showSuccessMessage) {
        LaunchedEffect(Unit) {
            delay(3000) // A mensagem será exibida por 3 segundos
            showSuccessMessage = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Room App Database", fontSize = 20.sp) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFDE29A1), // Cor de fundo da TopBar
                    titleContentColor = Color.White // Cor do texto da TopBar
                )
            )
        },
        content = { paddingValues ->
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .background(Color(0xFFF0F0F0)) // Cor de fundo clara
                    .padding(16.dp)
                    .fillMaxSize()
            ) {
                Spacer(modifier = Modifier.height(24.dp))

                // Mostrar mensagem de sucesso
                if (showSuccessMessage) {
                    Text(
                        text = "Cadastrado com sucesso!",
                        color = Color(0xFF28A745),
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(bottom = 16.dp)
                    )
                }

                // Campo de texto para Nome
                TextField(
                    value = nome,
                    onValueChange = { nome = it },
                    label = { Text("Nome") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.textFieldColors(
                        containerColor = Color.White,
                        focusedIndicatorColor = Color(0xFFDE29A1),
                        unfocusedIndicatorColor = Color(0xFFCCCCCC)
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Campo de texto para Telefone com máscara
                TextField(
                    value = telefone,
                    onValueChange = {
                        telefone = it
                    },
                    label = { Text("Telefone") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = TextFieldDefaults.textFieldColors(
                        containerColor = Color.White,
                        focusedIndicatorColor = Color(0xFFDE29A1),
                        unfocusedIndicatorColor = Color(0xFFCCCCCC)
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Botão centralizado com cor personalizada
                Button(
                    onClick = {
                        if (nome.isNotBlank() && telefone.isNotBlank()) {
                            viewModel.upsertPessoa(Pessoa(nome, telefone))
                            nome = ""
                            telefone = ""
                            showSuccessMessage = true // Inicia a exibição da mensagem
                        }
                    },
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFDE29A1), // Cor de fundo do botão
                        contentColor = Color.White  // Cor do texto do botão
                    )
                ) {
                    Text("Cadastrar")
                }

                Spacer(modifier = Modifier.height(16.dp))

                Divider()

                LazyColumn {
                    items(pessoaList) { pessoa ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = pessoa.nome,
                                modifier = Modifier.weight(1f),
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(
                                text = pessoa.telefone,
                                modifier = Modifier.weight(1f),
                                color = Color.Gray
                            )
                        }
                        Divider()
                    }
                }
            }
        }
    )
}
