package com.INSIGHTERS_PERU.Up.Work.Perusalen;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Prueba mínima de arranque del contexto de Spring.
 *
 * Usa el perfil "test" para no depender de las variables de Supabase,
 * Docker, SendGrid, S3 o Culqi configuradas en producción.
 */
@ActiveProfiles("test")
@SpringBootTest
class UpWorkPerusalenApplicationTests {

    @Test
    void contextLoads() {
        // Si Spring puede crear todo el ApplicationContext, la prueba pasa.
    }
}
