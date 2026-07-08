import { publicApi } from './api';
import type { MetadataCatalog } from '../types/metadata';

export async function getMetadata(locale = 'pt-BR') {
  const response = await publicApi.get<MetadataCatalog>('/metadata', {
    headers: {
      'Accept-Language': locale,
    },
  });
  return response.data;
}
