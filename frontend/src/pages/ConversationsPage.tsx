import CheckIcon from '@mui/icons-material/Check';
import DoneAllIcon from '@mui/icons-material/DoneAll';
import ErrorOutlineIcon from '@mui/icons-material/ErrorOutline';
import MarkChatUnreadIcon from '@mui/icons-material/MarkChatUnread';
import WhatsAppIcon from '@mui/icons-material/WhatsApp';
import {
  Avatar,
  Badge,
  Box,
  Chip,
  Divider,
  LinearProgress,
  List,
  ListItemAvatar,
  ListItemButton,
  ListItemText,
  Paper,
  Stack,
  Typography,
} from '@mui/material';
import { useQuery, useQueryClient } from '@tanstack/react-query';
import { useEffect, useMemo, useState } from 'react';
import { useMetadata } from '../hooks/useMetadata';
import { listConversationMessages, listConversations } from '../services/conversationService';
import type { ConversationMessage, ConversationSummary } from '../types/message';

function formatPhone(phone: string) {
  const digits = phone.replace(/\D/g, '');
  if (digits.length === 13 && digits.startsWith('55')) {
    return `+55 (${digits.slice(2, 4)}) ${digits.slice(4, 9)}-${digits.slice(9)}`;
  }
  if (digits.length === 11) {
    return `(${digits.slice(0, 2)}) ${digits.slice(2, 7)}-${digits.slice(7)}`;
  }
  return phone;
}

function formatInteraction(value: string) {
  return new Intl.DateTimeFormat('pt-BR', {
    day: '2-digit',
    month: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
  }).format(new Date(value));
}

function formatTime(value: string) {
  return new Intl.DateTimeFormat('pt-BR', {
    hour: '2-digit',
    minute: '2-digit',
  }).format(new Date(value));
}

function displayName(conversation: ConversationSummary) {
  return conversation.leadName ?? conversation.contactDisplayName ?? formatPhone(conversation.phone);
}

function messageText(message: ConversationMessage) {
  if (message.content) {
    return message.content;
  }
  if (message.type === 'IMAGE') {
    return 'Imagem';
  }
  if (message.type === 'AUDIO') {
    return 'Audio';
  }
  if (message.type === 'DOCUMENT') {
    return 'Documento';
  }
  if (message.type === 'TEMPLATE') {
    return 'Template enviado';
  }
  return 'Mensagem sem texto';
}

function lastMessage(conversation: ConversationSummary) {
  if (conversation.lastMessageContent) {
    return conversation.lastMessageContent;
  }
  if (conversation.lastMessageType === 'IMAGE') {
    return 'Imagem';
  }
  if (conversation.lastMessageType === 'AUDIO') {
    return 'Audio';
  }
  if (conversation.lastMessageType === 'DOCUMENT') {
    return 'Documento';
  }
  if (conversation.lastMessageType === 'TEMPLATE') {
    return 'Template enviado';
  }
  return 'Sem mensagens registradas';
}

function statusIcon(status: ConversationMessage['status']) {
  if (status === 'FAILED') {
    return <ErrorOutlineIcon fontSize="inherit" />;
  }
  if (status === 'DELIVERED' || status === 'READ') {
    return <DoneAllIcon fontSize="inherit" />;
  }
  return <CheckIcon fontSize="inherit" />;
}

export function ConversationsPage() {
  const metadata = useMetadata();
  const queryClient = useQueryClient();
  const [selectedConversationId, setSelectedConversationId] = useState<string | null>(null);

  const conversationsQuery = useQuery({
    queryKey: ['conversations'],
    queryFn: listConversations,
    refetchInterval: 5000,
  });

  const conversations = useMemo(() => conversationsQuery.data ?? [], [conversationsQuery.data]);
  const selectedConversation = useMemo(
    () => conversations.find((conversation) => conversation.id === selectedConversationId) ?? null,
    [conversations, selectedConversationId],
  );
  const unreadTotal = conversations.reduce((total, conversation) => total + conversation.unreadCount, 0);

  const messagesQuery = useQuery({
    queryKey: ['conversationMessages', selectedConversationId],
    queryFn: () => listConversationMessages(selectedConversationId as string),
    enabled: selectedConversationId !== null,
    refetchInterval: selectedConversationId ? 5000 : false,
  });

  const messages = messagesQuery.data ?? [];

  useEffect(() => {
    if (!selectedConversationId && conversations.length > 0) {
      setSelectedConversationId(conversations[0].id);
      return;
    }
    if (selectedConversationId && conversations.length > 0 && !selectedConversation) {
      setSelectedConversationId(conversations[0].id);
    }
  }, [conversations, selectedConversation, selectedConversationId]);

  useEffect(() => {
    if (selectedConversationId && messagesQuery.data) {
      void queryClient.invalidateQueries({ queryKey: ['conversations'] });
    }
  }, [messagesQuery.data, queryClient, selectedConversationId]);

  return (
    <Box sx={{ display: 'grid', gap: 3 }}>
      <Stack direction={{ xs: 'column', md: 'row' }} justifyContent="space-between" spacing={2}>
        <Box>
          <Typography component="h2" variant="h4" fontWeight={800}>
            Conversas
          </Typography>
          <Typography color="text.secondary" variant="body1">
            WhatsApp
          </Typography>
        </Box>

        <Stack direction="row" spacing={1}>
          <Chip icon={<WhatsAppIcon />} label={`${conversations.length} conversas`} variant="outlined" />
          <Chip
            color={unreadTotal > 0 ? 'warning' : 'default'}
            icon={<MarkChatUnreadIcon />}
            label={`${unreadTotal} nao lidas`}
            variant={unreadTotal > 0 ? 'filled' : 'outlined'}
          />
        </Stack>
      </Stack>

      {(conversationsQuery.isLoading || messagesQuery.isFetching) && <LinearProgress />}

      <Paper
        variant="outlined"
        sx={{
          borderRadius: 1,
          display: 'grid',
          gridTemplateColumns: { xs: '1fr', lg: 'minmax(300px, 380px) minmax(0, 1fr)' },
          minHeight: { xs: 620, lg: 'calc(100vh - 220px)' },
          overflow: 'hidden',
        }}
      >
        <Box sx={{ borderRight: { lg: 1 }, borderColor: 'divider', minWidth: 0 }}>
          <List disablePadding>
            {conversations.map((conversation) => {
              const hasUnread = conversation.unreadCount > 0;
              const selected = conversation.id === selectedConversationId;
              return (
                <ListItemButton
                  key={conversation.id}
                  divider
                  selected={selected}
                  onClick={() => setSelectedConversationId(conversation.id)}
                  sx={{
                    alignItems: 'flex-start',
                    gap: 1,
                    minHeight: 96,
                    bgcolor: hasUnread ? 'rgba(237, 108, 2, 0.08)' : 'background.paper',
                    '&.Mui-selected': {
                      bgcolor: 'action.selected',
                    },
                    '&:hover': {
                      bgcolor: hasUnread ? 'rgba(237, 108, 2, 0.14)' : 'action.hover',
                    },
                  }}
                >
                  <ListItemAvatar>
                    <Badge color="warning" badgeContent={conversation.unreadCount} invisible={!hasUnread} overlap="circular">
                      <Avatar sx={{ bgcolor: hasUnread ? 'warning.main' : 'primary.main' }}>
                        <WhatsAppIcon />
                      </Avatar>
                    </Badge>
                  </ListItemAvatar>
                  <ListItemText
                    primary={
                      <Stack direction={{ xs: 'column', sm: 'row' }} justifyContent="space-between" spacing={1}>
                        <Typography fontWeight={hasUnread ? 800 : 700} noWrap>
                          {displayName(conversation)}
                        </Typography>
                        <Typography color={hasUnread ? 'warning.dark' : 'text.secondary'} fontWeight={hasUnread ? 800 : 500} variant="body2">
                          {formatInteraction(conversation.lastInteractionAt)}
                        </Typography>
                      </Stack>
                    }
                    secondary={
                      <Box sx={{ display: 'grid', gap: 0.5, mt: 0.5 }}>
                        <Typography component="span" color="text.secondary" variant="body2">
                          {formatPhone(conversation.phone)}
                        </Typography>
                        <Typography
                          component="span"
                          color={hasUnread ? 'text.primary' : 'text.secondary'}
                          fontWeight={hasUnread ? 700 : 400}
                          sx={{
                            display: '-webkit-box',
                            overflow: 'hidden',
                            WebkitBoxOrient: 'vertical',
                            WebkitLineClamp: 1,
                          }}
                          variant="body2"
                        >
                          {lastMessage(conversation)}
                        </Typography>
                      </Box>
                    }
                  />
                </ListItemButton>
              );
            })}
            {!conversationsQuery.isLoading && conversations.length === 0 && (
              <Box sx={{ p: 3 }}>
                <Typography color="text.secondary">Nenhuma conversa encontrada.</Typography>
              </Box>
            )}
          </List>
        </Box>

        <Box sx={{ display: 'grid', gridTemplateRows: 'auto minmax(0, 1fr)', minWidth: 0 }}>
          {selectedConversation ? (
            <>
              <Stack spacing={0.5} sx={{ borderBottom: 1, borderColor: 'divider', px: 3, py: 2 }}>
                <Typography fontWeight={800} variant="h6" noWrap>
                  {displayName(selectedConversation)}
                </Typography>
                <Stack direction="row" spacing={1} useFlexGap flexWrap="wrap">
                  <Chip label={formatPhone(selectedConversation.phone)} size="small" variant="outlined" />
                  {selectedConversation.leadName && <Chip label="Lead vinculado" size="small" color="primary" variant="outlined" />}
                </Stack>
              </Stack>

              <Box
                sx={{
                  bgcolor: 'grey.50',
                  display: 'flex',
                  flexDirection: 'column',
                  gap: 1.25,
                  overflowY: 'auto',
                  px: { xs: 2, md: 3 },
                  py: 3,
                }}
              >
                {messages.map((message, index) => {
                  const outbound = message.direction === 'OUTBOUND';
                  return (
                    <Box
                      key={message.id}
                      sx={{
                        alignSelf: outbound ? 'flex-end' : 'flex-start',
                        maxWidth: { xs: '88%', md: '68%' },
                        minWidth: 120,
                      }}
                    >
                      {index > 0 && messages[index - 1].createdAt.slice(0, 10) !== message.createdAt.slice(0, 10) && (
                        <Divider sx={{ my: 2 }}>
                          <Typography color="text.secondary" variant="caption">
                            {new Intl.DateTimeFormat('pt-BR', { day: '2-digit', month: 'long' }).format(new Date(message.createdAt))}
                          </Typography>
                        </Divider>
                      )}
                      <Box
                        sx={{
                          bgcolor: outbound ? 'primary.main' : 'background.paper',
                          border: 1,
                          borderColor: outbound ? 'primary.main' : 'divider',
                          borderRadius: 1,
                          color: outbound ? 'primary.contrastText' : 'text.primary',
                          px: 1.5,
                          py: 1,
                          boxShadow: outbound ? 0 : 1,
                        }}
                      >
                        <Typography sx={{ whiteSpace: 'pre-wrap', overflowWrap: 'anywhere' }} variant="body2">
                          {messageText(message)}
                        </Typography>
                        <Stack direction="row" spacing={0.75} alignItems="center" justifyContent="flex-end" sx={{ mt: 0.75 }}>
                          <Typography color={outbound ? 'primary.contrastText' : 'text.secondary'} sx={{ opacity: outbound ? 0.82 : 1 }} variant="caption">
                            {formatTime(message.createdAt)}
                          </Typography>
                          {outbound && (
                            <Chip
                              icon={statusIcon(message.status)}
                              label={metadata.label('conversationMessageStatuses', message.status)}
                              size="small"
                              sx={{
                                bgcolor: message.status === 'FAILED' ? 'error.main' : 'rgba(255, 255, 255, 0.16)',
                                color: 'primary.contrastText',
                                height: 22,
                                '& .MuiChip-icon': {
                                  color: 'inherit',
                                },
                              }}
                            />
                          )}
                        </Stack>
                      </Box>
                    </Box>
                  );
                })}

                {!messagesQuery.isLoading && messages.length === 0 && (
                  <Box sx={{ alignSelf: 'center', mt: 8 }}>
                    <Typography color="text.secondary">Sem mensagens registradas.</Typography>
                  </Box>
                )}
              </Box>
            </>
          ) : (
            <Box sx={{ alignItems: 'center', display: 'flex', justifyContent: 'center', minHeight: 360, p: 3 }}>
              <Typography color="text.secondary">Nenhuma conversa selecionada.</Typography>
            </Box>
          )}
        </Box>
      </Paper>
    </Box>
  );
}
