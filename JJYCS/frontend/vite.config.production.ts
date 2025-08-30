import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { resolve } from 'path'
import { VitePWA } from 'vite-plugin-pwa'
import { visualizer } from 'rollup-plugin-visualizer'
import { compression } from 'vite-plugin-compression'

// Production-optimized Vite configuration for YSC LMS
export default defineConfig({
  plugins: [
    vue({
      template: {
        compilerOptions: {
          // Remove comments and whitespace in production
          comments: false,
          whitespace: 'condense'
        }
      }
    }),
    
    // PWA Configuration
    VitePWA({
      registerType: 'autoUpdate',
      workbox: {
        // Cache strategy for API calls
        runtimeCaching: [
          {
            urlPattern: /^https:\/\/api\.ycs\.com\/.*/i,
            handler: 'NetworkFirst',
            options: {
              cacheName: 'api-cache',
              expiration: {
                maxEntries: 100,
                maxAgeSeconds: 60 * 60 * 24 // 24 hours
              },
              cacheableResponse: {
                statuses: [0, 200]
              }
            }
          },
          {
            urlPattern: /\.(?:png|jpg|jpeg|svg|gif|webp)$/,
            handler: 'CacheFirst',
            options: {
              cacheName: 'images-cache',
              expiration: {
                maxEntries: 200,
                maxAgeSeconds: 60 * 60 * 24 * 30 // 30 days
              }
            }
          }
        ],
        globPatterns: [
          '**/*.{js,css,html,ico,png,svg,webp}'
        ],
        maximumFileSizeToCacheInBytes: 3000000 // 3MB
      },
      manifest: {
        name: 'YSC LMS',
        short_name: 'YSC LMS',
        description: 'YSC Logistics Management System',
        theme_color: '#1f2937',
        background_color: '#ffffff',
        display: 'standalone',
        orientation: 'portrait',
        scope: '/',
        start_url: '/',
        icons: [
          {
            src: '/pwa-192x192.png',
            sizes: '192x192',
            type: 'image/png'
          },
          {
            src: '/pwa-512x512.png',
            sizes: '512x512',
            type: 'image/png'
          },
          {
            src: '/pwa-512x512.png',
            sizes: '512x512',
            type: 'image/png',
            purpose: 'any maskable'
          }
        ]
      }
    }),
    
    // Bundle analyzer
    visualizer({
      filename: 'dist/stats.html',
      open: false,
      gzipSize: true,
      brotliSize: true
    }),
    
    // Gzip compression
    compression({
      algorithm: 'gzip',
      ext: '.gz'
    }),
    
    // Brotli compression
    compression({
      algorithm: 'brotliCompress',
      ext: '.br'
    })
  ],
  
  // Path resolution
  resolve: {
    alias: {
      '@': resolve(__dirname, 'src'),
      '@components': resolve(__dirname, 'src/components'),
      '@modules': resolve(__dirname, 'src/modules'),
      '@stores': resolve(__dirname, 'src/stores'),
      '@utils': resolve(__dirname, 'src/utils'),
      '@composables': resolve(__dirname, 'src/composables'),
      '@assets': resolve(__dirname, 'src/assets')
    }
  },
  
  // Development server (not used in production build)
  server: {
    port: 3003,
    host: true,
    cors: true
  },
  
  // Production build configuration
  build: {
    // Output directory
    outDir: 'dist',
    
    // Enable minification
    minify: 'terser',
    
    // Terser options
    terserOptions: {
      compress: {
        drop_console: true,
        drop_debugger: true,
        pure_funcs: ['console.log', 'console.info', 'console.debug'],
        unsafe_comps: true,
        unsafe_math: true,
        passes: 2
      },
      mangle: {
        safari10: true,
        properties: {
          regex: /^_/
        }
      },
      format: {
        comments: false,
        webkit: true
      }
    },
    
    // Rollup options
    rollupOptions: {
      output: {
        // Manual chunking strategy for better caching
        manualChunks: {
          // Vendor libraries
          'vendor-vue': ['vue', 'vue-router', 'pinia'],
          'vendor-ui': ['@headlessui/vue', '@heroicons/vue'],
          'vendor-utils': ['axios', 'date-fns'],
          
          // Feature-based chunks
          'auth': [
            './src/modules/auth/views/LoginPage.vue',
            './src/modules/auth/views/RegisterPage.vue',
            './src/modules/auth/store/authStore.ts'
          ],
          'orders': [
            './src/modules/orders/views/CreateOrderPage.vue',
            './src/modules/orders/views/OrderListPage.vue',
            './src/modules/orders/store/orderStore.ts'
          ],
          'warehouse': [
            './src/modules/warehouse/views/ScanPage.vue',
            './src/modules/warehouse/views/InventoryPage.vue',
            './src/modules/warehouse/store/warehouseStore.ts'
          ],
          'admin': [
            './src/modules/admin/views/ApprovalDashboard.vue',
            './src/modules/admin/views/UserManagement.vue',
            './src/modules/admin/store/adminStore.ts'
          ]
        },
        
        // Asset file naming
        chunkFileNames: 'assets/[name]-[hash].js',
        entryFileNames: 'assets/[name]-[hash].js',
        assetFileNames: (assetInfo) => {
          const info = assetInfo.name.split('.')
          const ext = info[info.length - 1]
          if (/\.(css)$/.test(assetInfo.name)) {
            return `assets/css/[name]-[hash].${ext}`
          }
          if (/\.(png|jpe?g|gif|svg|webp|ico)$/.test(assetInfo.name)) {
            return `assets/images/[name]-[hash].${ext}`
          }
          if (/\.(woff2?|eot|ttf|otf)$/.test(assetInfo.name)) {
            return `assets/fonts/[name]-[hash].${ext}`
          }
          return `assets/[name]-[hash].${ext}`
        }
      },
      
      // External dependencies (if using CDN)
      external: process.env.NODE_ENV === 'production' && process.env.USE_CDN === 'true' 
        ? ['vue', 'vue-router', 'pinia']
        : []
    },
    
    // Source map configuration
    sourcemap: process.env.GENERATE_SOURCEMAP === 'true',
    
    // Asset inlining threshold
    assetsInlineLimit: 4096, // 4KB
    
    // CSS code splitting
    cssCodeSplit: true,
    
    // Chunk size warnings
    chunkSizeWarningLimit: 1000, // 1MB
    
    // Asset optimization
    assetsDir: 'assets',
    
    // Report compressed sizes
    reportCompressedSize: true,
    
    // Write bundle info to file
    write: true,
    
    // Empty output directory before build
    emptyOutDir: true
  },
  
  // CSS configuration
  css: {
    // CSS modules configuration
    modules: {
      localsConvention: 'camelCase',
      generateScopedName: process.env.NODE_ENV === 'development' 
        ? '[name]__[local]__[hash:base64:5]'
        : '[hash:base64:5]'
    },
    
    // PostCSS plugins
    postcss: {
      plugins: [
        require('tailwindcss'),
        require('autoprefixer'),
        ...(process.env.NODE_ENV === 'production' ? [
          require('cssnano')({
            preset: ['default', {
              discardComments: {
                removeAll: true
              },
              normalizeWhitespace: false
            }]
          })
        ] : [])
      ]
    },
    
    // CSS preprocessing options
    preprocessorOptions: {
      scss: {
        additionalData: `
          @import "@/styles/variables.scss";
          @import "@/styles/mixins.scss";
        `
      }
    }
  },
  
  // Optimization hints
  optimizeDeps: {
    include: [
      'vue',
      'vue-router',
      'pinia',
      'axios',
      '@headlessui/vue',
      '@heroicons/vue/24/outline',
      '@heroicons/vue/24/solid',
      'date-fns'
    ],
    exclude: [
      'vue-demi'
    ]
  },
  
  // Environment variables
  define: {
    __VUE_OPTIONS_API__: false,
    __VUE_PROD_DEVTOOLS__: false,
    __APP_VERSION__: JSON.stringify(process.env.npm_package_version),
    __BUILD_TIME__: JSON.stringify(new Date().toISOString())
  },
  
  // Preview server configuration
  preview: {
    port: 5000,
    host: true,
    cors: true,
    headers: {
      'Cache-Control': 'public, max-age=31536000'
    }
  },
  
  // SSR configuration (if needed)
  ssr: {
    noExternal: ['@headlessui/vue']
  }
})

// Export type for TypeScript
export type ViteConfig = typeof defineConfig