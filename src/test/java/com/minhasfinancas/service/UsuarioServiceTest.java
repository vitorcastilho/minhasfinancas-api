package com.minhasfinancas.service;

import java.util.Optional;
import static org.assertj.core.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.minhasfinancas.exception.ErroAutenticacao;
import com.minhasfinancas.exception.RegraNegocioException;
import com.minhasfinancas.model.entity.Usuario;
import com.minhasfinancas.model.repository.UsuarioRepository;
import com.minhasfinancas.service.impl.UsuarioServiceImpl;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
public class UsuarioServiceTest {
 
	@SpyBean
	UsuarioServiceImpl service;

	@MockBean
	UsuarioRepository repository;

	@Test
	public void deveSalavarUmUsuario() {
		// cenario
		Mockito.doNothing().when(service).validarEmail(Mockito.anyString());
		Usuario usuario = Usuario.builder()
									.id(1l)
									.nome("nome")
									.email("email@email.com")
									.senha("senha")
									.build();

		Mockito.when(repository.save(Mockito.any(Usuario.class))).thenReturn(usuario);

		// acao
		Usuario usuarioSalvo = service.salvarUsuario(new Usuario());

		// verificacao
		assertThat(usuarioSalvo).isNotNull();
		assertThat(usuarioSalvo.getId()).isEqualTo(1l);
		assertThat(usuarioSalvo.getNome()).isEqualTo("nome");
		assertThat(usuarioSalvo.getEmail()).isEqualTo("email@email.com");
		assertThat(usuarioSalvo.getSenha()).isEqualTo("senha");
	}

	@Test
	public void naoDeveSalvarUmUsuarioComEmailJaCadastrado() {

		// Cenario
		String email = "email@email.com";
		Usuario usuario = Usuario.builder().email(email).build();
		Mockito.doThrow(RegraNegocioException.class).when(service).validarEmail(email);

		// acao
		Throwable exception = catchThrowable( () -> service.salvarUsuario(usuario));
		
		// verificacao
		Mockito.verify(repository, Mockito.never()).save(usuario);
	}

	@Test
	public void deveAutenticarUmUsuarioComSucesso() {
		// Cenario
		String email = "email@email.com";
		String senha = "senha";

		Usuario usuario = Usuario.builder().email(email).senha(senha).id(1l).build();
		Mockito.when(repository.findByEmail(email)).thenReturn(Optional.of(usuario));

		// acao
		Usuario result = service.autenticar(email, senha);

		// verificacao
		assertThat(result);
	}

	@Test
	public void deveLancarErroQuandoNaoEncontrarUsuarioCadastradoComEmailInformado() {
			// cenario
			Mockito.when(repository.findByEmail(Mockito.anyString())).thenReturn(Optional.empty());

			// acao
			Throwable exception = catchThrowable( () -> service.autenticar("email@email.com", "senha") );

			// verificacao
			assertThat(exception).isInstanceOf(ErroAutenticacao.class).hasMessage("Usuario não encontrado para o email informado.");
	}

	@Test
	public void deveLancarErroQuandoSenhaNaoBater() {
		// cenario
		String senha = "senha";
		Usuario usuario = Usuario.builder().email("email@email.com").senha(senha).build();
		Mockito.when(repository.findByEmail(Mockito.anyString())).thenReturn(Optional.of(usuario));

		// acao
		Throwable exception = catchThrowable( () -> service.autenticar("email@email.com", "123") );

		assertThat(exception).isInstanceOf(ErroAutenticacao.class).hasMessage("Senha inválida.");
	}

	@Test
	public void deveValidarEmail() {
		// cenario
		Mockito.when(repository.existsByEmail(Mockito.anyString())).thenReturn(false);

		// acao
		service.validarEmail("email@email.com");
	}

	@Test
	public void deveLancarErroAoValidarEmailQuandoExistirEmailCadastrado() {

		// cenario
		Mockito.when(repository.existsByEmail(Mockito.anyString())).thenReturn(true);

		// acao
		Throwable exception = catchThrowable( () -> service.validarEmail("email@email.com") );
		
		assertThat(exception).isInstanceOf(RegraNegocioException.class).hasMessage("Já existe um usuário cadastrado com este email.");
	}
}
