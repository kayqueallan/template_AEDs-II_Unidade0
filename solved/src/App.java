import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.IOException;
import java.util.Scanner;

public class App {

    /** Quantidade máxima de produtos que podem ser armazenados no vetor */
    static final int MAX_NOVOS_PRODUTOS = 10;

    /** Nome do arquivo de dados. O arquivo deve estar localizado na raiz do projeto */
    static String nomeArquivoDados;
    
    /** Scanner para leitura de dados do teclado */
    static Scanner teclado;

    /** Vetor de produtos cadastrados */
    static Produto[] produtosCadastrados;

    /** Quantidade de produtos cadastrados atualmente no vetor */
    static int quantosProdutos = 0;

    /** Gera um efeito de pausa na CLI. Espera por um enter para continuar */
    static void pausa() {
        System.out.println("Digite enter para continuar...");
        teclado.nextLine();
    }

    /** Cabeçalho principal da CLI do sistema */
    static void cabecalho() {
        System.out.println("AEDs II COMÉRCIO DE COISINHAS");
        System.out.println("=============================");
    }
    
    /** Imprime o menu principal, lê a opção do usuário e a retorna (int).
     * @return Um inteiro com a opção do usuário.
    */
    static int menu() {
        cabecalho();
        System.out.println("1 - Listar todos os produtos");
        System.out.println("2 - Procurar e imprimir os dados de um produto");
        System.out.println("3 - Cadastrar novo produto");
        System.out.println("0 - Sair");
        System.out.print("Digite sua opção: ");
        return Integer.parseInt(teclado.nextLine());
    }
    
    /**
     * Lê os dados de um arquivo-texto e retorna um vetor de produtos. Arquivo-texto no formato
     * N (quantidade de produtos) <br/>
     * tipo;descrição;preçoDeCusto;margemDeLucro;[dataDeValidade] <br/>
     * Deve haver uma linha para cada um dos produtos. Retorna um vetor vazio em caso de problemas com o arquivo.
     * @param nomeArquivoDados Nome do arquivo de dados a ser aberto.
     * @return Um vetor com os produtos carregados, ou vazio em caso de problemas de leitura.
     */
    static Produto[] lerProdutos(String nomeArquivoDados) {
        try {
            java.util.List<String> linhas = Files.readAllLines(
                    Paths.get(nomeArquivoDados), Charset.forName("UTF-8"));

            if (linhas.isEmpty()) return new Produto[0];

            int quantidade = Integer.parseInt(linhas.get(0).trim());
            if (quantidade < 0 || linhas.size() < quantidade + 1) return new Produto[0];

            Produto[] produtos = new Produto[quantidade];
            for (int i = 0; i < quantidade; i++) {
                produtos[i] = Produto.criarDoTexto(linhas.get(i + 1).trim());
            }
            return produtos;
        } catch (Exception e) {
            System.out.println("Erro ao ler arquivo de produtos: " + e.getMessage());
            return new Produto[0];
        }
    }
    
    /** Localiza um produto no vetor de produtos cadastrados, a partir do nome de produto informado pelo usuário, e imprime seus dados. 
     *  A busca não é sensível ao caso. Em caso de não encontrar o produto, imprime uma mensagem padrão */
    static void localizarProdutos() {
        System.out.print("Digite a descrição do produto: ");
        String busca = teclado.nextLine();

        for (int i = 0; i < quantosProdutos; i++) {
            if (produtosCadastrados[i].descricao.equalsIgnoreCase(busca)) {
                System.out.println(produtosCadastrados[i]);
                return;
            }
        }

        System.out.println("Produto não encontrado.");
    }
    
    /**
     * Salva os dados dos produtos cadastrados no arquivo csv informado. Sobrescreve todo o conteúdo do arquivo.
     * @param nomeArquivo Nome do arquivo a ser gravado.
     */
    public static void salvarProdutos(String nomeArquivo) {
        try {
            java.util.List<String> linhas = new java.util.ArrayList<>();
            linhas.add(String.valueOf(quantosProdutos));

            for (int i = 0; i < quantosProdutos; i++) {
                linhas.add(produtosCadastrados[i].gerarDadosTexto());
            }

            Files.write(Paths.get(nomeArquivo), linhas, Charset.forName("UTF-8"));
        } catch (IOException e) {
            System.out.println("Erro ao salvar arquivo de produtos: " + e.getMessage());
        }
    }
    
    /** Lista todos os produtos cadastrados, numerados, um por linha */
    static void listarTodosOsProdutos() {
        if (quantosProdutos == 0) {
            System.out.println("Nenhum produto cadastrado.");
            return;
        }

        for (int i = 0; i < quantosProdutos; i++) {
            System.out.println((i + 1) + " - " + produtosCadastrados[i]);
        }
    }
    
    /**
     * Rotina para cadastro de um novo produto: pergunta ao usuário o tipo do produto, lê os dados correspondentes,
     * cria o objeto adequado de acordo com o tipo, inclui o produto no vetor.
     */
    static void cadastrarProduto() {
        if (quantosProdutos >= produtosCadastrados.length) {
            System.out.println("Limite de produtos atingido.");
            return;
        }

        try {
            System.out.print("Tipo do produto (1 - não perecível / 2 - perecível): ");
            int tipo = Integer.parseInt(teclado.nextLine());

            System.out.print("Descrição: ");
            String descricao = teclado.nextLine();

            System.out.print("Preço de custo: ");
            double preco = Double.parseDouble(teclado.nextLine().replace(',', '.'));

            System.out.print("Margem de lucro: ");
            double margem = Double.parseDouble(teclado.nextLine().replace(',', '.'));

            Produto novoProduto;

            if (tipo == 1) {
                novoProduto = new ProdutoNaoPerecivel(descricao, preco, margem);
            } else if (tipo == 2) {
                System.out.print("Data de validade (dd/MM/yyyy): ");
                java.time.LocalDate validade = java.time.LocalDate.parse(
                        teclado.nextLine(),
                        java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                novoProduto = new ProdutoPerecivel(descricao, preco, margem, validade);
            } else {
                System.out.println("Tipo de produto inválido.");
                return;
            }

            for (int i = 0; i < quantosProdutos; i++) {
                if (produtosCadastrados[i].equals(novoProduto)) {
                    System.out.println("Já existe um produto com essa descrição.");
                    return;
                }
            }

            produtosCadastrados[quantosProdutos++] = novoProduto;
            System.out.println("Produto cadastrado com sucesso.");
        } catch (Exception e) {
            System.out.println("Não foi possível cadastrar o produto: " + e.getMessage());
        }
    }  
    
	public static void main(String[] args) {
		teclado = new Scanner(System.in, Charset.forName("UTF-8"));
        nomeArquivoDados = "dadosProdutos.csv";

        Produto[] produtosLidos = lerProdutos(nomeArquivoDados);
        produtosCadastrados = new Produto[produtosLidos.length + MAX_NOVOS_PRODUTOS];
        for (int i = 0; i < produtosLidos.length; i++) {
            produtosCadastrados[i] = produtosLidos[i];
        }
        quantosProdutos = produtosLidos.length;

        int opcao = -1;
      
        do{
            opcao = menu();
            switch (opcao) {
                case 1 -> listarTodosOsProdutos();
                case 2 -> localizarProdutos();
                case 3 -> cadastrarProduto();
                case 0 -> System.out.println("Encerrando...");
                default -> System.out.println("Opção inválida.");
            }
            if (opcao != 0) pausa();
        }while(opcao != 0);       

        salvarProdutos(nomeArquivoDados);
        teclado.close();    
    }
}
