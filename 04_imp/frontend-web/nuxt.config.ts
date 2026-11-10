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
      title: 'CNS · Médico',
      htmlAttrs: { lang: 'es' },
      meta: [{ name: 'viewport', content: 'width=device-width, initial-scale=1' }],
    },
  },
  runtimeConfig: {
    public: {
      apiUrl: 'http://localhost:8080',
      usarFakes: false,
    },
  },
})
