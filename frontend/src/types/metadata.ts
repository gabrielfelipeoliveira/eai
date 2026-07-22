export type MetadataCollection =
  | 'leadStatuses'
  | 'leadSources'
  | 'followUpStatuses'
  | 'userRoles'
  | 'userStatuses'
  | 'tenantStatuses'
  | 'messageTemplateTypes'
  | 'messageTemplateMetaStatuses'
  | 'leadDistributionModes'
  | 'emailAccountStatuses'
  | 'emailProtocols'
  | 'conversationMessageDirections'
  | 'conversationMessageTypes'
  | 'conversationMessageStatuses';

export interface MetadataOption {
  code: string;
  labelKey: string;
  label: string;
  order: number;
  color: string;
}

export type MetadataCatalog = {
  locale: string;
} & Record<MetadataCollection, MetadataOption[]>;
