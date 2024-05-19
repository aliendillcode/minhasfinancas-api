package com.dill.minhasfinancas.api.resource;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import com.dill.minhasfinancas.api.dto.UsuarioDTO;
import com.dill.minhasfinancas.exceptions.ErroAutenticacao;
import com.dill.minhasfinancas.exceptions.RegraNegocioException;
import com.dill.minhasfinancas.model.entity.Usuario;
import com.dill.minhasfinancas.service.LancamentoService;
import com.dill.minhasfinancas.service.UsuarioService;
import com.fasterxml.jackson.databind.ObjectMapper;


@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
@WebMvcTest( controllers = UsuarioResource.class )
@AutoConfigureMockMvc
public class UsuarioResourceTest {
	
	static final String API = "/api/usuarios";
	static final MediaType JSON = MediaType.APPLICATION_JSON;
	
	@Autowired
	MockMvc mvc;
	
	@MockBean
	UsuarioService service;
	
	@MockBean
	LancamentoService lancamentoService;
	
	@Test
	public void deveAutenticarUmUsuario() throws Exception {
		//cenario
		String email = "usuario@email.com";
		String senha = "123";		
		
		UsuarioDTO dto = UsuarioDTO.builder().email(email).senha(senha).build();
		Usuario usuario = Usuario.builder().id(1l).email(email).senha(senha).build();
		Mockito.when( service.autenticar(email, senha) ).thenReturn(usuario);
		String json = new ObjectMapper().writeValueAsString(dto);
		
		//execucao e verificacao
		MockHttpServletRequestBuilder request = postRequestAutenticar(json, "/autenticar");
		
		
		mvc
			.perform(request)
			.andExpect( MockMvcResultMatchers.status().isOk()  )
			.andExpect( MockMvcResultMatchers.jsonPath("id").value(usuario.getId())  )
			.andExpect( MockMvcResultMatchers.jsonPath("nome").value(usuario.getNome())  )
			.andExpect( MockMvcResultMatchers.jsonPath("email").value(usuario.getEmail())  )	
		;
	}

	
	@Test
	public void deveRetornarBadRequestAoObterErroDeAutenticacao() throws Exception {
		//cenario
		String email = "usuario@email.com";
		String senha = "123";		
		
		UsuarioDTO dto = UsuarioDTO.builder().email(email).senha(senha).build();
		
		Mockito.when( service.autenticar(email, senha) ).thenThrow(ErroAutenticacao.class);
		
		String json = new ObjectMapper().writeValueAsString(dto);
		
		//execucao e verificacao
		MockHttpServletRequestBuilder request = postRequestAutenticar(json, "/autenticar");
		
		
		mvc
			.perform(request)
			.andExpect( MockMvcResultMatchers.status().isBadRequest()  );
	}
	
	@Test
	public void deveCriarUmUsuario() throws Exception {
		//cenário
		String email = "usuario@email.com";
		String nome = "usuario a salvar";
		String senha = "123";
		
		UsuarioDTO dto = UsuarioDTO.builder().email(email).nome(nome).senha(senha).build();
		Usuario usuario = Usuario.builder().id(1l).email(email).nome(nome).senha(senha).build();
		Mockito.when( service.salvarUsuario(Mockito.any(Usuario.class)) ).thenReturn(usuario);
		String json = new ObjectMapper().writeValueAsString(dto);
		
		//execucao e verificacao
		MockHttpServletRequestBuilder request = postRequestAutenticar(json, "");
		
		mvc
			.perform(request)
			.andExpect(MockMvcResultMatchers.status().isCreated())
			.andExpect( MockMvcResultMatchers.jsonPath("id").value(usuario.getId())  )
			.andExpect( MockMvcResultMatchers.jsonPath("nome").value(usuario.getNome())  )
			.andExpect( MockMvcResultMatchers.jsonPath("email").value(usuario.getEmail())  );
		
	}
	
	@Test
	public void deveRetornarBadRequestAotentarCriarUmUsuarioInvalido() throws Exception {
		//cenário
				String email = "usuario@email.com";
				String nome = "usuario a salvar";
				String senha = "123";
				
				UsuarioDTO dto = UsuarioDTO.builder().email(email).nome(nome).senha(senha).build();
				
				Mockito.when( service.salvarUsuario(Mockito.any(Usuario.class)) ).thenThrow(RegraNegocioException.class);
				String json = new ObjectMapper().writeValueAsString(dto);
				
				//execucao e verificacao
				MockHttpServletRequestBuilder request = postRequestAutenticar(json, "");
				
				mvc
				.perform(request)
				.andExpect( MockMvcResultMatchers.status().isBadRequest()  );
	}
	
	
	
	
	
	private MockHttpServletRequestBuilder postRequestAutenticar(String json, String endPoint) {
		MockHttpServletRequestBuilder request = MockMvcRequestBuilders
													.post( API.concat(endPoint) )
													.accept( JSON )
													.contentType( JSON )
													.content(json);
		return request;
	}
	
}
