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

}
