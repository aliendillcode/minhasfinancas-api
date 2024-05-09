package com.dill.minhasfinancas.service;


import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.ActiveProfiles;

import com.dill.minhasfinancas.exceptions.ErroAutenticacao;
import com.dill.minhasfinancas.exceptions.RegraNegocioException;
import com.dill.minhasfinancas.model.entity.Usuario;
import com.dill.minhasfinancas.model.repository.UsuarioRepository;
import com.dill.minhasfinancas.service.impl.UsuarioServiceImpl;

@SpringBootTest
@ActiveProfiles("test")
public class UsuarioServiceTest {
	
	@SpyBean
	UsuarioServiceImpl service;	
	
	@MockBean
	UsuarioRepository repository;
	

	
	@Test
	public void deveSalvarUmUsuario() {
		//cenário
		Mockito.doNothing().when(service).validarEmail(Mockito.anyString());
		Usuario usuario = Usuario.builder()
				.id(1l)
				.nome("nome")
				.email("email@email.com")
				.senha("senha").build();
		Mockito.when(repository.save(Mockito.any(Usuario.class))).thenReturn(usuario);	
		
		//ação
		Usuario usuarioSalvo = service.salvarUsuario(new Usuario());
		
		assertThat(usuarioSalvo).isNotNull();
		assertThat(usuarioSalvo.getId()).isEqualTo(1l);
		assertThat(usuarioSalvo.getNome()).isEqualTo("nome");
		assertThat(usuarioSalvo.getEmail()).isEqualTo("email@email.com");
		assertThat(usuarioSalvo.getSenha()).isEqualTo("senha");
	}
	
	@Test
	public void itDoesNotSaveAnUserUsingARegisterdEmail() {
		//scenario
		String email = "email@email.com";
		Usuario usuario = Usuario.builder().email(email).build();
		Mockito.doThrow(RegraNegocioException.class).when(service).validarEmail(email);
		
		assertThrows(RegraNegocioException.class, ()-> {
			//action
			service.salvarUsuario(usuario);
			
			//check
			Mockito.verify(repository, Mockito.never()).save(usuario);
		});
	}
	
	@Test
	public void itMustAuthenticateAUserSuccessfuly() {		
			//scenario
			String email = "email@email.com";
			String senha = "senha123";
			
			Usuario usuario = Usuario.builder().email(email).senha(senha).id(1l).build();
			Mockito.when(repository.findByEmail(email)).thenReturn(Optional.of(usuario));
			
			//action
			Usuario result = service.autenticar(email, senha);
			
			//check
			assertNotNull(result);
;
	}
	//deveLancarErroQuandoNaoEncontrarUsuarioCadastradoComOEmailInformado
	@Test
	public void itShouldThownAnErrorWhenItDoesNotFindARegisterdUserUsingTheEmailProvided () {
		//cenário
		Mockito.when(repository.findByEmail(Mockito.anyString())).thenReturn(Optional.empty());
		
		//açao
		RuntimeException runtimeException = assertThrows(
				ErroAutenticacao.class,()-> 
				service.autenticar("email@email.com", "senha")
			);
		//verificação
		assertTrue(runtimeException.getMessage().equals("Usuário não encontrado para o email informado"));
		
	}
	
	@Test
	public void deveLancarErroQuandoSenhaNaoBater() {
		//cenário
		String senha = "senha";
		Usuario usuario = Usuario.builder().email("email@mail.com").senha(senha).build();
		Mockito.when(repository.findByEmail(Mockito.anyString())).thenReturn(Optional.of(usuario));
		
		//ação
		RuntimeException runtimeException = assertThrows(
				ErroAutenticacao.class,()-> service.autenticar("email@mail.com", "123"));
		assertTrue(runtimeException.getMessage().equals("Senha inválida"));
	}
	
	
	
	@Test
	public void deveValidarEmail() {
		//cenário			
		Mockito.when(repository.existsByEmail(Mockito.anyString())).thenReturn(false);
		
		//ação
		assertDoesNotThrow(()-> service.validarEmail("eamail@email.com"));
	}
	
	@Test
	public void deveLancarErroAoValidarEmailQuantoExistirEmailCadastrado() {
		//cenário
		Mockito.when(repository.existsByEmail(Mockito.anyString())).thenReturn(true);
		
		//ação
		assertThrows(RegraNegocioException.class, ()->service.validarEmail("email@email.com"));
	}
}
