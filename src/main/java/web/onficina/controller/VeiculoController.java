package web.onficina.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.SortDefault;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import io.github.wimdeblauwe.htmx.spring.boot.mvc.HxRequest;

import org.springframework.ui.Model;
import org.springframework.data.domain.Sort;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import web.onficina.filter.VeiculoFilter;
import web.onficina.model.modelOnficina.Usuario;
import web.onficina.model.modelOnficina.Veiculo;
import web.onficina.pagination.PageWrapper;
import web.onficina.repository.VeiculoRepository;
import web.onficina.service.VeiculoService;

@Controller
@RequestMapping("/painel/veiculo")
public class VeiculoController {

    private static final Logger logger = LoggerFactory.getLogger(VacinaController.class);

    private VeiculoRepository veiculoRepository;
    private VeiculoService veiculoService;

    public VeiculoController(VeiculoRepository veiculoRepository, VeiculoService veiculoService) {
        this.veiculoRepository = veiculoRepository;
        this.veiculoService = veiculoService;
    }

    @GetMapping("/cadastrar")
    public String mostrarFormularioCadastro(Model model) {
        model.addAttribute("veiculo", new Veiculo());

        return "veiculo/cadastrar :: formulario";
    }

    @PostMapping("/cadastrar")
    public String salvar(@Valid @ModelAttribute Veiculo veiculo, 
            BindingResult result, 
            Model model,
            RedirectAttributes redirectAttributes,
            HttpSession session) {
        if (result.hasErrors()) {
            return "veiculo/cadastrar :: formulario";
        }

        Usuario usuarioLogado = (Usuario) session.getAttribute("usuarioLogado");

        if (usuarioLogado == null) {
            redirectAttributes.addFlashAttribute("erro", "Usuário não está logado.");
            return "redirect:/login";
        }

        veiculo.setProprietario(usuarioLogado);

        veiculoService.salvar(veiculo);
        model.addAttribute("mensagem", "Veículo cadastrado com sucesso!");
        return "redirect:/veiculo/listar";
    }

    @HxRequest
    @GetMapping("/listar")
    public String listar(VeiculoFilter filtro, Model model,
            @PageableDefault(size = 7) @SortDefault(sort = "id", direction = Sort.Direction.ASC) Pageable pageable,
            HttpServletRequest request) {

        Page<Veiculo> pagina = veiculoRepository.pesquisar(filtro, pageable);
        PageWrapper<Veiculo> paginaWrapper = new PageWrapper<>(pagina, request);

        model.addAttribute("pagina", paginaWrapper);
        return "veiculo/listar :: tabela";
    }

    @HxRequest
    @GetMapping("/pesquisar")
    public String pesquisar(VeiculoFilter filtro, Model model,
            @PageableDefault(size = 5) @SortDefault(sort = "id", direction = Sort.Direction.ASC) Pageable pageable,
            HttpServletRequest request) {

        Page<Veiculo> pagina = veiculoRepository.pesquisar(filtro, pageable);
        PageWrapper<Veiculo> paginaWrapper = new PageWrapper<>(pagina, request);

        model.addAttribute("pagina", paginaWrapper);
        return "veiculo/listar :: tabela";
    }

    @GetMapping("/alterar/{id}")
    public String mostrarFormularioAlterar(@PathVariable Long id, Model model, HttpSession session, RedirectAttributes redirectAttributes) {
        Usuario usuarioLogado = (Usuario) session.getAttribute("usuarioLogado");
        if (usuarioLogado == null) {
            redirectAttributes.addFlashAttribute("erro", "Usuário não está logado.");
            return "redirect:/login";
        }

        Veiculo veiculo = veiculoRepository.findById(id).orElse(null);

        if (veiculo == null) {
            redirectAttributes.addFlashAttribute("erro", "Veículo não encontrado.");
            return "redirect:/painel/veiculo/listar";
        }
        
        // Verifica se o veículo pertence ao usuário logado
        if (!veiculo.getProprietario().getId().equals(usuarioLogado.getId())) {
            redirectAttributes.addFlashAttribute("erro", "Este veículo não pertence ao usuário logado.");
            return "redirect:/painel/veiculo/listar";
        }

        model.addAttribute("veiculo", veiculo);
        return "veiculo/alterar"; // Assuming this view exists
    }

    @PostMapping("/alterar/{id}")
    public String alterar(@PathVariable Long id, 
                          @Valid @ModelAttribute Veiculo veiculoForm, 
                          BindingResult result, 
                          RedirectAttributes redirectAttributes, 
                          HttpSession session,
                          Model model) {

        if (result.hasErrors()) {
            model.addAttribute("veiculo", veiculoForm); // Add veiculoForm back to model to display errors
            return "veiculo/alterar";
        }

        Usuario usuarioLogado = (Usuario) session.getAttribute("usuarioLogado");
        if (usuarioLogado == null) {
            redirectAttributes.addFlashAttribute("erro", "Usuário não está logado.");
            return "redirect:/login";
        }

        Veiculo veiculoExistente = veiculoRepository.findById(id).orElse(null);

        if (veiculoExistente == null) {
            redirectAttributes.addFlashAttribute("erro", "Veículo não encontrado.");
            return "redirect:/painel/veiculo/listar";
        }

        // Verifica se o veículo pertence ao usuário logado
        if (!veiculoExistente.getProprietario().getId().equals(usuarioLogado.getId())) {
            redirectAttributes.addFlashAttribute("erro", "Este veículo não pertence ao usuário logado e não pode ser alterado.");
            return "redirect:/painel/veiculo/listar";
        }

        // Atualiza os campos do veículo existente com os dados do formulário
        veiculoExistente.setPlaca(veiculoForm.getPlaca());
        veiculoExistente.setMarca(veiculoForm.getMarca());
        veiculoExistente.setModelo(veiculoForm.getModelo());
        veiculoExistente.setAno(veiculoForm.getAno());
        veiculoExistente.setCor(veiculoForm.getCor());
        // Define o proprietário como o usuário logado, conforme instruído para seguir a lógica do 'salvar' e por simplicidade.
        // A verificação de propriedade anterior já garante que usuarioLogado é o proprietário.
        veiculoExistente.setProprietario(usuarioLogado);

        try {
            veiculoService.salvar(veiculoExistente); // Salva o veículo atualizado
            redirectAttributes.addFlashAttribute("mensagem", "Veículo alterado com sucesso!");
        } catch (Exception e) {
            logger.error("Erro ao alterar veículo: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("erro", "Erro ao alterar veículo.");
            model.addAttribute("veiculo", veiculoForm);
            return "veiculo/alterar";
        }

        return "redirect:/painel/veiculo/listar";
    }

    @GetMapping("/remover/{id}")
    public String remover(@PathVariable Long id, RedirectAttributes redirectAttributes, HttpSession session) {
        Usuario usuarioLogado = (Usuario) session.getAttribute("usuarioLogado");
        if (usuarioLogado == null) {
            redirectAttributes.addFlashAttribute("erro", "Usuário não está logado.");
            return "redirect:/login";
        }

        Veiculo veiculoParaRemover = veiculoRepository.findById(id).orElse(null);

        if (veiculoParaRemover == null) {
            redirectAttributes.addFlashAttribute("erro", "Veículo não encontrado.");
            return "redirect:/painel/veiculo/listar";
        }

        // Important Security Check: Verify ownership
        if (!veiculoParaRemover.getProprietario().getId().equals(usuarioLogado.getId())) {
            redirectAttributes.addFlashAttribute("erro", "Você não tem permissão para remover este veículo.");
            return "redirect:/painel/veiculo/listar";
        }

        try {
            veiculoRepository.deleteById(id);
            redirectAttributes.addFlashAttribute("mensagem", "Veículo removido com sucesso!");
        } catch (Exception e) {
            logger.error("Erro ao remover veículo com ID {}: {}", id, e.getMessage());
            redirectAttributes.addFlashAttribute("erro", "Erro ao remover veículo. Verifique se ele não está associado a outros registros.");
        }

        return "redirect:/painel/veiculo/listar";
    }
}
