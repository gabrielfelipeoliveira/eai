export type MetadataCollection =
  | 'leadStatuses'
  | 'leadSources'
  | 'followUpStatuses'
  | 'userRoles'
  | 'userStatuses'
  | 'tenantStatuses'
  | 'messageTemplateTypes'
  | 'leadDistributionModes'
  | 'emailAccountStatuses'
  | 'emailProtocols';

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
