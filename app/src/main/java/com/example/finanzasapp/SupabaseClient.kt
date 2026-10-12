package com.example.finanzasapp
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest

object SupabaseClient {
    val client = createSupabaseClient(
        supabaseUrl = "https://jpkpvtkrmnwqsdlhptgs.supabase.co", // Tu Project URL de Supabase
        supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Impwa3B2dGtybW53cXNkbGhwdGdzIiwicm9sZSI6ImFub24iLCJpYXQiOjE3OTE3NjQ1ODMsImV4cCI6MjEwNzM0MDU4M30.aKh2HU0yxVGCX2emG1MCVnt7oVNOTNGBfQtHWvpP9i0"                 // Tu anon/public key de Supabase
    ) {
        install(Postgrest)
    }
}