// next.config.js
import createNextIntlPlugin from 'next-intl/plugin';

const withNextIntl = createNextIntlPlugin({
  // Custom path to request config
  requestConfig: './src/i18n/request.ts'
});

const nextConfig = {};

export default withNextIntl(nextConfig);