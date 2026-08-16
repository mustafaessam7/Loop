package com.example.ui.dialogs

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.repository.ChatMessage
import com.example.data.repository.GeminiRestManager
import com.example.data.repository.MessageSender
import com.example.ui.theme.LoopAmberSecondary
import com.example.ui.theme.LoopDangerRed
import com.example.ui.theme.LoopTealDark
import com.example.ui.theme.LoopTealPrimary
import kotlinx.coroutines.launch

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AiChatDialog(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    var inputPrompt by remember { mutableStateOf("") }
    var isGenerating by remember { mutableStateOf(false) }

    val chatMessages = remember {
        mutableStateListOf(
            ChatMessage(
                sender = MessageSender.AI_ASSISTANT,
                text = "أهلاً بك في المساعد الذكي لنظام Loop 🤖!\nأنا هنا لمساعدتك في تشخيص الأعطال الفنية للسيارات، نصائح الصيانة وتبديل الدهن، وإرشادات إدارة الفواتير والديون. كيف يمكنني مساعدتك اليوم؟"
            )
        )
    }

    val quickPrompts = listOf(
        "تشخيص كود عطل P0300 (ميسفاير المحرك)",
        "جدول مواعيد تبديل زيت وفلاتر السيارات",
        "كيفية تسجيل فاتورة صيانة ودين زبون",
        "نصيحة لزيادة مبيعات الورشة وتنظيم الفحوصات"
    )

    fun sendPrompt(promptText: String) {
        val trimmed = promptText.trim()
        if (trimmed.isBlank() || isGenerating) return

        val userMessage = ChatMessage(sender = MessageSender.USER, text = trimmed)
        chatMessages.add(userMessage)
        inputPrompt = ""
        isGenerating = true

        // Placeholder for streaming AI response
        val aiMessageId = System.currentTimeMillis().toString()
        val aiPlaceholder = ChatMessage(
            id = aiMessageId,
            sender = MessageSender.AI_ASSISTANT,
            text = "جاري التفكير وتحليل الطلب..."
        )
        chatMessages.add(aiPlaceholder)

        scope.launch {
            try {
                GeminiRestManager.sendMessageStream(chatMessages.dropLast(1), trimmed).collect { streamedText ->
                    val index = chatMessages.indexOfFirst { it.id == aiMessageId }
                    if (index != -1) {
                        chatMessages[index] = chatMessages[index].copy(
                            text = streamedText,
                            isError = streamedText.startsWith("⚠️")
                        )
                    }
                }
            } catch (e: Exception) {
                val index = chatMessages.indexOfFirst { it.id == aiMessageId }
                if (index != -1) {
                    chatMessages[index] = chatMessages[index].copy(
                        text = "⚠️ تعذر استقبال الإجابة بالكامل. يرجى التحقق من الاتصال بالإنترنت وإعادة المحاولة.",
                        isError = true
                    )
                }
            } finally {
                isGenerating = false
            }
        }
    }

    // Scroll to bottom when new messages arrive
    LaunchedEffect(chatMessages.size) {
        if (chatMessages.isNotEmpty()) {
            listState.animateScrollToItem(chatMessages.size - 1)
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.90f)
                .testTag("ai_chat_dialog"),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Header Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(LoopTealPrimary.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.SmartToy,
                                contentDescription = null,
                                tint = LoopTealPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "المساعد الذكي للورشة 🤖",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(LoopAmberSecondary.copy(alpha = 0.2f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "Gemini 3.5",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = LoopAmberSecondary
                                    )
                                }
                            }
                            Text(
                                text = "دعم صيانة السيارات والتشخيص الميكانيكي وإدارة الورش",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = {
                                chatMessages.clear()
                                chatMessages.add(
                                    ChatMessage(
                                        sender = MessageSender.AI_ASSISTANT,
                                        text = "تم البدء بمحادثة جديدة. كيف يمكنني مساعدتك؟"
                                    )
                                )
                            }
                        ) {
                            Icon(Icons.Default.Clear, contentDescription = "مسح المحادثة", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }

                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "إغلاق", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Chat Messages Scrollable Area
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(chatMessages, key = { it.id }) { msg ->
                        val isUser = msg.sender == MessageSender.USER
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
                        ) {
                            Card(
                                shape = RoundedCornerShape(
                                    topStart = 16.dp,
                                    topEnd = 16.dp,
                                    bottomStart = if (isUser) 16.dp else 2.dp,
                                    bottomEnd = if (isUser) 2.dp else 16.dp
                                ),
                                colors = CardDefaults.cardColors(
                                    containerColor = when {
                                        msg.isError -> LoopDangerRed.copy(alpha = 0.12f)
                                        isUser -> LoopTealPrimary
                                        else -> MaterialTheme.colorScheme.surfaceVariant
                                    }
                                ),
                                modifier = Modifier.fillMaxWidth(0.85f)
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = if (isUser) "أنت" else "المساعد الذكي 🤖",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isUser) Color.White.copy(alpha = 0.9f) else LoopTealPrimary
                                        )

                                        if (!isUser) {
                                            IconButton(
                                                onClick = {
                                                    clipboardManager.setText(AnnotatedString(msg.text))
                                                    Toast.makeText(context, "تم نسخ النص 📋", Toast.LENGTH_SHORT).show()
                                                },
                                                modifier = Modifier.size(20.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.ContentCopy,
                                                    contentDescription = "نسخ",
                                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                            }
                                        }
                                    }

                                    Text(
                                        text = msg.text,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = when {
                                            msg.isError -> LoopDangerRed
                                            isUser -> Color.White
                                            else -> MaterialTheme.colorScheme.onSurface
                                        },
                                        fontSize = 13.sp,
                                        lineHeight = 19.sp
                                    )
                                }
                            }
                        }
                    }
                }

                // Quick Suggestion Chips
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                ) {
                    quickPrompts.forEach { prompt ->
                        FilterChip(
                            selected = false,
                            onClick = { sendPrompt(prompt) },
                            label = { Text(prompt, fontSize = 11.sp) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    modifier = Modifier.size(12.dp),
                                    tint = LoopAmberSecondary
                                )
                            },
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }

                // Bottom Input Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = inputPrompt,
                        onValueChange = { inputPrompt = it },
                        placeholder = { Text("اسأل عن الأعطال الميكانيكية، الدهن، أو الفواتير...") },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("ai_chat_input"),
                        singleLine = false,
                        maxLines = 3,
                        shape = RoundedCornerShape(14.dp)
                    )

                    Button(
                        onClick = { sendPrompt(inputPrompt) },
                        enabled = !isGenerating && inputPrompt.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = LoopTealPrimary),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .size(52.dp)
                            .testTag("ai_chat_send_button")
                    ) {
                        if (isGenerating) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
                        } else {
                            Icon(
                                imageVector = Icons.Default.Send,
                                contentDescription = "إرسال",
                                tint = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}
