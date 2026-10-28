package com.mx.cbtis.bromitas;

import static org.mockito.Mockito.mockStatic;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.SpringBootTest;

import com.mx.cbtis.Ejercicio5ClienteApplication;

@SpringBootTest(classes = Ejercicio5ClienteApplication.class)
class Ejercicio5ClienteApplicationTests {

	@Test
	void contextLoads() {
	}

	@Test
	void mainArrancaLaAplicacion() {
		// Se simula SpringApplication.run para verificar el punto de entrada sin levantar otro servidor
		try (MockedStatic<SpringApplication> spring = mockStatic(SpringApplication.class)) {
			String[] args = { "--server.port=0" };
			Ejercicio5ClienteApplication.main(args);
			spring.verify(() -> SpringApplication.run(Ejercicio5ClienteApplication.class, args));
		}
	}

}
