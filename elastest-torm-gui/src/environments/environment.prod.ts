export const environment: { production: boolean, hostElasticSearch: string, eus: string } = {
  production: true,
  hostElasticSearch: window.location.hostname + ':9200',
  eus: window.location.hostname + ':8040'
};
