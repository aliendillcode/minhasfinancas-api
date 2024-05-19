package com.dill.minhasfinancas.service;


import static org.assertj.core.api.Assertions.*;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.assertj.core.api.Assertions;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.data.domain.Example;
import org.springframework.test.context.ActiveProfiles;

import com.dill.minhasfinancas.exceptions.RegraNegocioException;
import com.dill.minhasfinancas.model.entity.Lancamento;
import com.dill.minhasfinancas.model.entity.Usuario;
import com.dill.minhasfinancas.model.enums.StatusLancamento;
import com.dill.minhasfinancas.model.repository.LancamentoRepository;
import com.dill.minhasfinancas.model.repository.LancamentoRepositoryTest;
import com.dill.minhasfinancas.service.impl.LancamentoServiceImpl;


@SpringBootTest
@ActiveProfiles("test")
public class LancamentoServiceTest {
	@SpyBean
	LancamentoServiceImpl service;
	@MockBean
	LancamentoRepository repository;
	
	@Test
	public void deveSalvarUmLancamento() {  
		//cenário
		Lancamento lancamentoASalvar = LancamentoRepositoryTest.criarLancamento();
		Mockito.doNothing().when(service).validar(lancamentoASalvar);
		
		Lancamento lancamentoSalvo = LancamentoRepositoryTest.criarLancamento();
		lancamentoSalvo.setId(1l);
		lancamentoSalvo.setStatus(StatusLancamento.PENDENTE);
		Mockito.when(repository.save(lancamentoASalvar)).thenReturn(lancamentoSalvo);
		
		//execução
		Lancamento lancamento = service.salvar(lancamentoASalvar);
		
		assertThat(lancamento.getId()).isEqualTo(lancamentoSalvo.getId());
		assertThat(lancamento.getStatus()).isEqualTo(StatusLancamento.PENDENTE);
		
	}
	
	@Test
	public void naoDeveSalvarUmLancamentoQuandoHouverErroDeValidacao() {
		//cenário
		Lancamento lancamentoASalvar = LancamentoRepositoryTest.criarLancamento();
		Mockito.doThrow(RegraNegocioException.class).when(service).validar(lancamentoASalvar);
		
		
		catchThrowableOfType(() -> service.salvar(lancamentoASalvar), RegraNegocioException.class);
		Mockito.verify(repository, Mockito.never()).save(lancamentoASalvar);
		
	}
	
	@Test
	public void deveAtualizarUmLancamento() {  
		//cenário
		Lancamento lancamentoSalvo = LancamentoRepositoryTest.criarLancamento();
		lancamentoSalvo.setId(1l);
		lancamentoSalvo.setStatus(StatusLancamento.PENDENTE);
		
		Mockito.doNothing().when(service).validar(lancamentoSalvo);
		Mockito.when(repository.save(lancamentoSalvo)).thenReturn(lancamentoSalvo);
		
		//execução
		service.salvar(lancamentoSalvo);
		
		//verificação
		Mockito.verify(repository, Mockito.times(1)).save(lancamentoSalvo);	
		
	}
	
	@Test
	public void deveLancarErroAoTentarAtualizarUmLancamentoQueAindaNaoFoiSalvo() {  
		//cenário
		Lancamento lancamento= LancamentoRepositoryTest.criarLancamento();

		
		//execução e verificação		
		catchThrowableOfType(() -> service.atualizar(lancamento), NullPointerException.class);
		Mockito.verify(repository, Mockito.never()).save(lancamento);
	}
	
	@Test
	public void deveDeletarUmLancamento() {
		//cenário
		Lancamento lancamento = LancamentoRepositoryTest.criarLancamento();
		lancamento.setId(1l);
		
		//execução
		service.deletar(lancamento);
		
		//verificação
		Mockito.verify(repository).delete(lancamento);
	}
	
	@Test
	public void deveLancarErroaoTentarDeletarUmLancamentoQueAindaNaofoiSalvo() {
		//cenário
		Lancamento lancamento = LancamentoRepositoryTest.criarLancamento();
				
		//execução
		Assertions.catchThrowableOfType(()->service.deletar(lancamento), NullPointerException.class);
		
		//verficação
		Mockito.verify(repository, Mockito.never()).delete(lancamento);
	}
	
	@Test
	public void deveFiltrarLancamentos() {
		//cenário		
		Lancamento lancamento = LancamentoRepositoryTest.criarLancamento();
		lancamento.setId(1l);
		
		List<Lancamento> lista = Arrays.asList(lancamento);
		Mockito.when( repository.findAll(Mockito.any(Example.class)) ).thenReturn(lista);
		
		//execução
		List<Lancamento> resultado = service.buscar(lancamento);
		
		//verificação
		Assertions.assertThat(resultado)
			.isNotEmpty()
			.hasSize(1)
			.contains(lancamento);
		
	}
	
	@Test
	public void deveAtualizarOStatusDeUmLancamento() {
		//cnário
		Lancamento lancamento = LancamentoRepositoryTest.criarLancamento();
		lancamento.setId(1l);
		lancamento.setStatus(StatusLancamento.PENDENTE);
		
		StatusLancamento novoStatus = StatusLancamento.EFETIVADO;
		Mockito.doReturn(lancamento).when(service).atualizar(lancamento);
		
		//execução
		service.atualizarStatus(lancamento, novoStatus);
		
		//verificações
		Assertions.assertThat(lancamento.getStatus()).isEqualTo(novoStatus);
		Mockito.verify(service).atualizar(lancamento);
	}
	
	@Test
	public void deveObterUmLancamentoporId() {
		//cenário
		Long id = 1l;
		Lancamento lancamento = LancamentoRepositoryTest.criarLancamento();
		lancamento.setId(id);
		
		Mockito.when(repository.findById(id)).thenReturn(Optional.of(lancamento));
		
		//execução
		Optional<Lancamento> resultado = service.obterPorId(id);
		
		//verificação
		Assertions.assertThat(resultado.isPresent()).isTrue();
	}
	
	@Test
	public void deveRetornarVazioQuandoOLancamentoNaoExiste() {
		//cenário
		Long id = 1l;
		Lancamento lancamento = LancamentoRepositoryTest.criarLancamento();
		lancamento.setId(id);
		
		Mockito.when(repository.findById(id)).thenReturn(Optional.empty());
		
		//execução
		Optional<Lancamento> resultado = service.obterPorId(id);
		
		//verificação
		Assertions.assertThat(resultado.isPresent()).isFalse();
	}
	
	@Test
	public void deveLancarErrosAoValidarUmLancamento() {
		//cenário
		Lancamento lancamento = new Lancamento();		
		verifyError(lancamento, "Informe uma descrição válida");		
		lancamento.setDescricao("");
		verifyError(lancamento, "Informe uma descrição válida");
		
		lancamento.setDescricao("Salario");
		
		verifyError(lancamento, "Informe um Mês Válido");
		lancamento.setMes(0);
		verifyError(lancamento, "Informe um Mês Válido");
		lancamento.setMes(13);
		verifyError(lancamento, "Informe um Mês Válido");
		
		lancamento.setMes(1);
		
		verifyError(lancamento, "Informe um Ano válido");
		lancamento.setAno(123);
		verifyError(lancamento, "Informe um Ano válido");
		
		lancamento.setAno(2024);
		
		verifyError(lancamento, "Informe um usuário");	
		
		lancamento.setUsuario(new Usuario());
		lancamento.getUsuario().setId(1l);
		
		verifyError(lancamento, "Informe um valor válido");
		lancamento.setValor(BigDecimal.ZERO);
		verifyError(lancamento, "Informe um valor válido");

		
		lancamento.setValor(BigDecimal.valueOf(1));
		verifyError(lancamento, "Informe um tipo de lançamento");
	}

	private void verifyError(Lancamento lancamento, String message) {
		Throwable erro = Assertions.catchThrowable(()-> service.validar(lancamento));
		Assertions.assertThat(erro).isInstanceOf(RegraNegocioException.class).hasMessage(message);
	}
	

}
