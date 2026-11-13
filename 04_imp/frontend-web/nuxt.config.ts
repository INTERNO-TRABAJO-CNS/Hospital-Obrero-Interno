// Web del medico (T100-18). Aplicacion interna: sin SSR.
// La URL del backend sale de NUXT_PUBLIC_API_URL; NUXT_PUBLIC_USAR_FAKES=true trabaja sin backend.
export default defineNuxtConfig({
  compatibilityDate: '2025-07-15',
  ssr: false,
  devtools: { enabled: false },
  modules: ['@pinia/nuxt'],
  css: ['~/assets/css/base.css'],
  app: {
    head: {
      title: 'CNS · Web del médico',
      htmlAttrs: { lang: 'es' },
      meta: [{ name: 'viewport', content: 'width=device-width, initial-scale=1' }],
      link: [
        { rel: 'icon', type: 'image/jpeg', href: '/logo-cns.jpg' },
        { rel: 'preconnect', href: 'https://fonts.googleapis.com' },
        { rel: 'preconnect', href: 'https://fonts.gstatic.com', crossorigin: '' },
        { rel: 'stylesheet', href: 'https://fonts.googleapis.com/css2?family=Inter:wght@400;500;700;800&display=swap' },
      ],
    },
  },
  runtimeConfig: {
    public: {
      apiUrl: 'http://localhost:8080',
      usarFakes: false,
    },
  },
})
