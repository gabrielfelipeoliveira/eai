import MarkChatUnreadIcon from '@mui/icons-material/MarkChatUnread';
import WhatsAppIcon from '@mui/icons-material/WhatsApp';
import {
  Avatar,
  Badge,
  Box,
  Chip,
  LinearProgress,
  List,
  ListItemAvatar,
  ListItemButton,
  ListItemText,
  Paper,
  Stack,
  Typography,
} from '@mui/material';
import { useQuery } from '@tanstack/react-query';
import { listConversations } from '../services/conversationService';
import type { ConversationSummary } from '../types/message';

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

function displayName(conversation: ConversationSummary) {
  return conversation.leadName ?? conversation.contactDisplayName ?? formatPhone(conversation.phone);
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

export function ConversationsPage() {
  const conversationsQuery = useQuery({
    queryKey: ['conversations'],
    queryFn: listConversations,
  });

  const conversations = conversationsQuery.data ?? [];
  const unreadTotal = conversations.reduce((total, conversation) => total + conversation.unreadCount, 0);

  return (
    <Box sx={{ display: 'grid', gap: 3 }}>
      <Stack direction={{ xs: 'column', md: 'row' }} justifyContent="space-between" spacing={2}>
        <Box>
          <Typography component="h2" variant="h4" fontWeight={800}>
            Conversas
          </Typography>
          <Typography color="text.secondary" variant="body1">
            Atendimento WhatsApp dos leads sob responsabilidade do vendedor.
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

      {conversationsQuery.isLoading && <LinearProgress />}

      <Paper variant="outlined" sx={{ borderRadius: 1, overflow: 'hidden' }}>
        <List disablePadding>
          {conversations.map((conversation) => {
            const hasUnread = conversation.unreadCount > 0;
            return (
              <ListItemButton
                key={conversation.id}
                divider
                sx={{
                  alignItems: 'flex-start',
                  gap: 1,
                  bgcolor: hasUnread ? 'rgba(237, 108, 2, 0.08)' : 'background.paper',
                  '&:hover': {
                    bgcolor: hasUnread ? 'rgba(237, 108, 2, 0.14)' : 'action.hover',
                  },
                }}
              >
                <ListItemAvatar>
                  <Badge
                    color="warning"
                    badgeContent={conversation.unreadCount}
                    invisible={!hasUnread}
                    overlap="circular"
                  >
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
      </Paper>
    </Box>
  );
}
