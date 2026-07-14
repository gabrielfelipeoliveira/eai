import CheckIcon from '@mui/icons-material/Check';
import DoneAllIcon from '@mui/icons-material/DoneAll';
import ErrorOutlineIcon from '@mui/icons-material/ErrorOutline';
import FilterAltOffIcon from '@mui/icons-material/FilterAltOff';
import MarkChatUnreadIcon from '@mui/icons-material/MarkChatUnread';
import SendIcon from '@mui/icons-material/Send';
import WhatsAppIcon from '@mui/icons-material/WhatsApp';
import {
  Alert,
  Avatar,
  Badge,
  Box,
  Button,
  Chip,
  CircularProgress,
  Divider,
  LinearProgress,
  List,
  ListItemAvatar,
  ListItemButton,
  ListItemText,
  MenuItem,
  Paper,
  Stack,
  TextField,
  Typography,
} from '@mui/material';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { useEffect, useMemo, useState } from 'react';
import { useAuth } from '../hooks/useAuth';
import { useMetadata } from '../hooks/useMetadata';
import { listConversationMessages, listConversations, sendConversationTextMessage } from '../services/conversationService';
import { listActiveTemplates, sendWhatsappTemplate } from '../services/templateService';
import { listUsers } from '../services/userService';
import type { ConversationMessage, ConversationMessageStatus, ConversationSummary } from '../types/message';

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

function apiErrorCode(error: unknown) {
  return (error as { response?: { data?: { code?: string } } }).response?.data?.code;
}

function apiErrorMessage(error: unknown) {
  return (error as { response?: { data?: { message?: string } } }).response?.data?.message;
}

function dayStart(value: string) {
  return value ? new Date(`${value}T00:00:00`).toISOString() : undefined;
}

function dayEnd(value: string) {
  return value ? new Date(`${value}T23:59:59.999`).toISOString() : undefined;
}

export function ConversationsPage() {
  const metadata = useMetadata();
  const { hasAnyRole } = useAuth();
  const queryClient = useQueryClient();
  const [selectedConversationId, setSelectedConversationId] = useState<string | null>(null);
  const [composerText, setComposerText] = useState('');
  const [selectedTemplateId, setSelectedTemplateId] = useState('');
  const [sellerId, setSellerId] = useState('');
  const [messageStatus, setMessageStatus] = useState('');
  const [startDate, setStartDate] = useState('');
  const [endDate, setEndDate] = useState('');
  const canFilterBySeller = hasAnyRole(['ADMIN', 'MANAGER']);
  const conversationFilters = useMemo(
    () => ({
      sellerId: canFilterBySeller && sellerId ? sellerId : undefined,
      messageStatus: messageStatus ? (messageStatus as ConversationMessageStatus) : undefined,
      startAt: dayStart(startDate),
      endAt: dayEnd(endDate),
    }),
    [canFilterBySeller, endDate, messageStatus, sellerId, startDate],
  );

  const conversationsQuery = useQuery({
    queryKey: ['conversations', conversationFilters],
    queryFn: () => listConversations(conversationFilters),
    refetchInterval: 5000,
  });

  const usersQuery = useQuery({
    queryKey: ['conversationSellerFilterUsers'],
    queryFn: listUsers,
    enabled: canFilterBySeller,
  });

  const conversations = useMemo(() => conversationsQuery.data ?? [], [conversationsQuery.data]);
  const sellers = useMemo(
    () => (usersQuery.data ?? []).filter((user) => user.roles.includes('SELLER') && user.status === 'ACTIVE'),
    [usersQuery.data],
  );
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

  const templatesQuery = useQuery({
    queryKey: ['activeTemplates'],
    queryFn: listActiveTemplates,
    enabled: Boolean(selectedConversation?.leadId),
  });

  const sendTextMutation = useMutation({
    mutationFn: ({ conversationId, content }: { conversationId: string; content: string }) => sendConversationTextMessage(conversationId, content),
    onSuccess: (message) => {
      queryClient.setQueryData<ConversationMessage[]>(['conversationMessages', message.conversationId], (current) => [...(current ?? []), message]);
      setComposerText('');
      void queryClient.invalidateQueries({ queryKey: ['conversations'] });
    },
  });

  const sendTemplateMutation = useMutation({
    mutationFn: ({ leadId, templateId }: { leadId: string; templateId: string }) => sendWhatsappTemplate(leadId, templateId),
    onSuccess: (response) => {
      if (selectedConversationId) {
        const now = new Date().toISOString();
        const message: ConversationMessage = {
          id: response.conversationMessageId,
          conversationId: selectedConversationId,
          direction: 'OUTBOUND',
          type: 'TEMPLATE',
          status: response.status,
          externalMessageId: response.externalMessageId,
          content: response.message,
          mediaId: null,
          mediaMimeType: null,
          createdAt: now,
          updatedAt: now,
        };
        queryClient.setQueryData<ConversationMessage[]>(['conversationMessages', selectedConversationId], (current) => [...(current ?? []), message]);
      }
      setSelectedTemplateId('');
      void queryClient.invalidateQueries({ queryKey: ['conversations'] });
    },
  });

  const sendTextErrorBelongsToSelectedConversation = sendTextMutation.variables?.conversationId === selectedConversationId;
  const freeTextWindowExpired = sendTextErrorBelongsToSelectedConversation && apiErrorCode(sendTextMutation.error) === 'WHATSAPP_FREE_TEXT_WINDOW_EXPIRED';
  const canSendText = Boolean(selectedConversationId && composerText.trim()) && !sendTextMutation.isPending;

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

  useEffect(() => {
    setComposerText('');
    setSelectedTemplateId('');
  }, [selectedConversationId]);

  function handleSendText() {
    if (!selectedConversationId || !composerText.trim()) {
      return;
    }
    sendTextMutation.mutate({ conversationId: selectedConversationId, content: composerText });
  }

  function handleSendTemplate() {
    if (!selectedConversation?.leadId || !selectedTemplateId) {
      return;
    }
    sendTemplateMutation.mutate({ leadId: selectedConversation.leadId, templateId: selectedTemplateId });
  }

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

      <Paper variant="outlined" sx={{ borderRadius: 1, p: 2 }}>
        <Stack direction={{ xs: 'column', md: 'row' }} spacing={1.5}>
          {canFilterBySeller && (
            <TextField
              select
              label="Vendedor"
              size="small"
              value={sellerId}
              onChange={(event) => setSellerId(event.target.value)}
              sx={{ minWidth: { md: 220 } }}
            >
              <MenuItem value="">Todos</MenuItem>
              {sellers.map((seller) => (
                <MenuItem key={seller.id} value={seller.id}>
                  {seller.name}
                </MenuItem>
              ))}
            </TextField>
          )}
          <TextField
            select
            label="Status"
            size="small"
            value={messageStatus}
            onChange={(event) => setMessageStatus(event.target.value)}
            sx={{ minWidth: { md: 180 } }}
          >
            <MenuItem value="">Todos</MenuItem>
            {metadata.options('conversationMessageStatuses').map((option) => (
              <MenuItem key={option.code} value={option.code}>
                {option.label}
              </MenuItem>
            ))}
          </TextField>
          <TextField
            label="Inicio"
            size="small"
            type="date"
            value={startDate}
            onChange={(event) => setStartDate(event.target.value)}
            slotProps={{ inputLabel: { shrink: true } }}
          />
          <TextField
            label="Fim"
            size="small"
            type="date"
            value={endDate}
            onChange={(event) => setEndDate(event.target.value)}
            slotProps={{ inputLabel: { shrink: true } }}
          />
          <Button
            variant="outlined"
            startIcon={<FilterAltOffIcon />}
            onClick={() => {
              setSellerId('');
              setMessageStatus('');
              setStartDate('');
              setEndDate('');
            }}
            sx={{ minWidth: 96 }}
          >
            Limpar
          </Button>
        </Stack>
      </Paper>

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

        <Box sx={{ display: 'grid', gridTemplateRows: 'auto minmax(0, 1fr) auto', minWidth: 0 }}>
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

              <Box sx={{ borderTop: 1, borderColor: 'divider', bgcolor: 'background.paper', p: { xs: 2, md: 3 } }}>
                <Stack spacing={1.5}>
                  {freeTextWindowExpired && (
                    <Alert severity="warning">
                      Janela de 24 horas expirada. Use um template aprovado para retomar a conversa.
                    </Alert>
                  )}
                  {sendTextMutation.isError && sendTextErrorBelongsToSelectedConversation && !freeTextWindowExpired && (
                    <Alert severity="error">{apiErrorMessage(sendTextMutation.error) ?? 'Nao foi possivel enviar a mensagem.'}</Alert>
                  )}
                  {sendTemplateMutation.isError && (
                    <Alert severity="error">{apiErrorMessage(sendTemplateMutation.error) ?? 'Nao foi possivel enviar o template.'}</Alert>
                  )}

                  {freeTextWindowExpired && selectedConversation.leadId && (
                    <Stack direction={{ xs: 'column', sm: 'row' }} spacing={1}>
                      <TextField
                        select
                        fullWidth
                        label="Template"
                        size="small"
                        value={selectedTemplateId}
                        onChange={(event) => setSelectedTemplateId(event.target.value)}
                      >
                        {(templatesQuery.data ?? []).map((template) => (
                          <MenuItem key={template.id} value={template.id}>
                            {template.name}
                          </MenuItem>
                        ))}
                      </TextField>
                      <Button
                        variant="contained"
                        startIcon={sendTemplateMutation.isPending ? <CircularProgress color="inherit" size={16} /> : <SendIcon />}
                        disabled={!selectedTemplateId || sendTemplateMutation.isPending}
                        onClick={handleSendTemplate}
                        sx={{ minWidth: 160 }}
                      >
                        Template
                      </Button>
                    </Stack>
                  )}

                  {freeTextWindowExpired && !selectedConversation.leadId && (
                    <Alert severity="info">Esta conversa nao possui lead vinculado para envio de template.</Alert>
                  )}

                  <Stack direction={{ xs: 'column', sm: 'row' }} spacing={1}>
                    <TextField
                      fullWidth
                      multiline
                      maxRows={4}
                      minRows={1}
                      placeholder="Digite uma mensagem"
                      value={composerText}
                      onChange={(event) => {
                        setComposerText(event.target.value);
                        if (sendTextMutation.isError) {
                          sendTextMutation.reset();
                        }
                      }}
                      onKeyDown={(event) => {
                        if (event.key === 'Enter' && !event.shiftKey) {
                          event.preventDefault();
                          handleSendText();
                        }
                      }}
                    />
                    <Button
                      variant="contained"
                      endIcon={sendTextMutation.isPending ? <CircularProgress color="inherit" size={16} /> : <SendIcon />}
                      disabled={!canSendText}
                      onClick={handleSendText}
                      sx={{ minWidth: 132 }}
                    >
                      Enviar
                    </Button>
                  </Stack>
                </Stack>
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
