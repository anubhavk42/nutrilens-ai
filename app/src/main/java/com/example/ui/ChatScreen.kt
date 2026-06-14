package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.ChatMessage
import com.example.ui.theme.ForestGreen
import com.example.viewmodel.NutriViewModel
import com.example.ui.theme.*

@Composable
fun ChatScreen(viewModel: NutriViewModel) {
    val messages by viewModel.chatMessages.collectAsStateWithLifecycle()
    val isThinking = viewModel.isChatThinking

    var textInput by remember { mutableStateOf("") }
    val lazyListState = rememberLazyListState()

    LaunchedEffect(messages.size, isThinking) {
        if (messages.isNotEmpty()) {
            lazyListState.animateScrollToItem(messages.size - 1)
        }
    }

    val suggestionChips = listOf(
        "Recommend a high-protein dinner",
        "Explain carbs in dal tadka",
        "Is Poha good for diabetes goals?",
        "Indian vegetarian staples"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(OrganicBackground)
            .testTag("chat_screen")
    ) {
        // Compact NutriBot status bar - no duplicate header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(ForestGreen)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "NutriBot · Gemini powered",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 11.sp,
                        color = BentoTextGrey
                    )
                )
            }
            IconButton(onClick = { viewModel.clearChatHistory() }) {
                Icon(imageVector = Icons.Default.Refresh, contentDescription = "Clear Chat", tint = Color.Gray, modifier = Modifier.size(18.dp))
            }
        }
        Divider(color = Color(0xFFEBEFE9), thickness = 1.dp)

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            if (messages.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // NutriBot Avatar
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(ForestGreen),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "N",
                            style = MaterialTheme.typography.headlineLarge.copy(
                                fontSize = 36.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Namaste! I am NutriBot. 🙏",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Your personal South Asian nutrition guide, powered by Gemini AI",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = Color(0xFF615E58),
                            lineHeight = 20.sp
                        ),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(24.dp))

                    // Quick topic cards
                    val quickTopics = listOf(
                        Triple("🍗", "High Protein Dinner", "Recommend a high-protein dinner"),
                        Triple("🍲", "Dal Tadka Carbs", "Explain carbs in dal tadka"),
                        Triple("🥣", "Poha for Diabetes", "Is Poha good for diabetes goals?"),
                        Triple("🥗", "Veggie Staples", "Indian vegetarian staples")
                    )

                    quickTopics.forEach { (emoji, title, query) ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color.White)
                                .border(1.dp, BentoBorderGrey, RoundedCornerShape(16.dp))
                                .clickable { viewModel.sendBotChatMessage(query) }
                                .padding(14.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(text = emoji, fontSize = 24.sp)
                                Column {
                                    Text(
                                        text = title,
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    )
                                    Text(
                                        text = query,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = Color(0xFF6F7A72),
                                            fontSize = 11.sp
                                        )
                                    )
                                }
                                Spacer(modifier = Modifier.weight(1f))
                                Icon(
                                    imageVector = Icons.Default.ArrowForward,
                                    contentDescription = "Ask",
                                    tint = ForestGreen,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            } else {
                LazyColumn(
                    state = lazyListState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(messages) { msg ->
                        ChatBubbleRow(msg = msg)
                    }

                    if (isThinking) {
                        item {
                            ThinkingIndicatorRow()
                        }
                    }
                }
            }
        }

        if (messages.isNotEmpty()) {
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(suggestionChips) { itemText ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(999.dp))
                            .background(Color.White)
                            .border(1.dp, Color(0xFFE0E3DE), RoundedCornerShape(999.dp))
                            .clickable { viewModel.sendBotChatMessage(itemText) }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = itemText,
                            style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF615E58), fontSize = 11.sp)
                        )
                    }
                }
            }
        }

        Divider(color = Color(0xFFEBEFE9), thickness = 1.dp)

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = textInput,
                    onValueChange = { textInput = it },
                    placeholder = { Text("Ask NutriBot AI chatbot...") },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("chat_input_field"),
                    shape = RoundedCornerShape(999.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BentoPurpleMedium,
                        unfocusedBorderColor = BentoBorderGrey,
                        focusedContainerColor = BentoSecondaryContainer,
                        unfocusedContainerColor = Color.White
                    ),
                    maxLines = 3,
                    trailingIcon = {
                        if (textInput.isNotEmpty()) {
                            IconButton(onClick = { viewModel.sendBotChatMessage(textInput); textInput = "" }) {
                                Icon(
                                    imageVector = Icons.Default.Send,
                                    contentDescription = "Send text message",
                                    tint = BentoPurpleMedium
                                )
                            }
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun ChatBubbleRow(msg: ChatMessage) {
    val bubbleColor = if (msg.isBot) Color.White else BentoPurpleMedium
    val textColor = if (msg.isBot) MaterialTheme.colorScheme.onSurface else Color.White
    val contentAlign = if (msg.isBot) Alignment.Start else Alignment.End

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = contentAlign
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .clip(
                    RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (msg.isBot) 4.dp else 16.dp,
                        bottomEnd = if (msg.isBot) 16.dp else 4.dp
                    )
                )
                .background(bubbleColor)
                .border(
                    width = 1.dp,
                    color = if (msg.isBot) BentoBorderGrey else Color.Transparent,
                    shape = RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (msg.isBot) 4.dp else 16.dp,
                        bottomEnd = if (msg.isBot) 16.dp else 4.dp
                    )
                )
                .padding(14.dp)
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (msg.isBot) Icons.Default.Person else Icons.Default.Person,
                        contentDescription = "Sender",
                        tint = if (msg.isBot) BentoPurpleMedium else Color.White.copy(alpha = 0.8f),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (msg.isBot) "NutriBot Guide" else "You",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (msg.isBot) BentoTextGrey else Color.White.copy(alpha = 0.8f)
                        )
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = msg.message,
                    style = MaterialTheme.typography.bodyMedium.copy(color = textColor, fontSize = 15.sp)
                )
            }
        }
    }
}

@Composable
fun ThinkingIndicatorRow() {
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 4.dp, bottomEnd = 16.dp))
                .background(Color.White)
                .border(1.dp, BentoBorderGrey)
                .padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "NutriBot Active",
                    tint = BentoPurpleMedium,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "NutriBot is crafting advice",
                    style = MaterialTheme.typography.labelSmall.copy(color = BentoTextGrey, fontStyle = FontStyle.Italic)
                )
                Spacer(modifier = Modifier.width(8.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(BentoPurpleMedium.copy(alpha = 0.8f)))
                    Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(BentoPurpleMedium.copy(alpha = 0.6f)))
                    Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(BentoPurpleMedium.copy(alpha = 0.4f)))
                }
            }
        }
    }
}
