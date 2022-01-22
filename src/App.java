import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.List;
import java.util.Scanner;
import java.util.TimeZone;


public class App {
    public static Scanner input = new Scanner(System.in);
    private static final Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
    private static final int CURRENT_YEAR = cal.get(Calendar.YEAR);
    private static final int CURRENT_MONTH = cal.get(Calendar.MONTH)+1; //STARTS COUNTING ON 0 = JAN
    private static final String[] INTERVALS = {"year","month","week"};

    public static void main(String[] args) throws SQLException {
        checkAndFixRestrictions();
        while (true) {optionsMenu();}
    }

    private static void optionsMenu() throws SQLException {
        optionsMenuDisplay();
        switch (getValInt()) {
            case 1 -> novoAtivo();
            case 2 -> substituirElem();
            case 3 -> ativoForaServico();
            case 4 -> custoTotalAtivo();
            case 5 -> query2d();
            case 6 -> query2e();
            case 7 -> queries.query3c();
            case 8 -> query3d();
            case 9 -> exit();
            default -> System.err.println("Opção não reconhecida");
        }
    }

    private static void novoAtivo() throws SQLException {
        System.out.println("Inserir novo Ativo na Base de dados");
        System.out.println("Insira os seguintes dados relativos a um novo Ativo");
        System.out.println("Nome");
        String nome = getValString();
        System.out.println("Data Aquisição");
        String data = getDate();
        System.out.println("Marca");
        String marca = getValString();
        System.out.println("Modelo");
        String modelo = getValString();
        System.out.println("Localização");
        String local = getValString();
        System.out.println("Id Ativo Topo");
        System.out.println("Tem de corresponder a um dos Ativos já presentes no sistema");
        System.out.println("Nome ativos no sistema: ");
        List<String> ativos = queries.getAtivos();
        printList(ativos);
        String nomeAtivoTopo = checkIfInList(ativos);
        String idAtivoTopo = queries.getIdAtivo(nomeAtivoTopo);
        // tipo é inferido a partir da restrição 4 -> Ativos filhos são do mesmo tipo que o seu ativo pai (ativoTopo)
        int tipo = queries.getIdTipoFromAtivo(idAtivoTopo);
        System.out.println("Empresa");
        System.out.println("Apenas são aceites empresas já presentes no sistema");
        List<String> empresas = queries.getEmpresas();
        printList(empresas);
        String nomeEmpresa = checkIfInList(empresas);
        int empresa = queries.getIdEmpresa(nomeEmpresa);
        System.out.println("Pessoa");
        List<String> nomePessoas = queries.getNomePessoas();
        printList(nomePessoas);
        String nomePessoa = checkIfInList(nomePessoas);
        int pessoa = queries.getIdPessoa(nomePessoa);
        queries.novoAtivo(nome, data, marca, modelo, local, idAtivoTopo, tipo, empresa, pessoa);
    }

    private static void substituirElem() throws SQLException {
        System.out.println("Pessoa a ser substituída");
        List<String> nomePessoas = queries.getNomePessoas();
        printList(nomePessoas);
        String nomePessoaOut = checkIfInList(nomePessoas);
        int pessoaOut = queries.getIdPessoa(nomePessoaOut);
        System.out.println("Pessoa a substituir");
        String nomePessoaIn = checkIfInList(nomePessoas);
        int pessoaIn = queries.getIdPessoa(nomePessoaIn);
        queries.substituirElem(pessoaOut,pessoaIn);
    }

    private static void ativoForaServico() throws SQLException {
        System.out.println("Ativo");
        System.out.println("Ativos no sistema: ");
        List<String> ativos = queries.getAtivos();
        printList(ativos);
        String nomeAtivo = checkIfInList(ativos);
        String idAtivo = queries.getIdAtivo(nomeAtivo);
        queries.ativoForaServico(idAtivo);
    }

    private static void custoTotalAtivo() throws SQLException {
        System.out.println("Ativo");
        System.out.println("Ativos no sistema: ");
        List<String> ativos = queries.getAtivos();
        printList(ativos);
        String nomeAtivo = checkIfInList(ativos);
        String idAtivo = queries.getIdAtivo(nomeAtivo);
        System.out.println("Custo Total do Ativo: " + queries.custoTotalAtivo(idAtivo) + "€");
    }

    private static void query2d() throws SQLException {
        System.out.println("Ativo");
        System.out.println("Ativos no sistema: ");
        List<String> ativos = queries.getAtivos();
        printList(ativos);
        String nomeAtivo = checkIfInList(ativos);
        queries.query2d(nomeAtivo);
    }

    private static void query2e() throws SQLException {
        System.out.println("Pessoa");
        System.out.println("Pessoas no sistema: ");
        List<String> pessoas = queries.getNomePessoas();
        printList(pessoas);
        String nome = checkIfInList(pessoas);
        queries.query2e(nome);
    }

    private static void query3d() throws SQLException {
        System.out.println("Período");
        System.out.println("ex 1 month");
        System.out.println("Quantidade");
        int amount = getValInt();
        System.out.println("Tipo de Intervalo");
        System.out.println("tipos de intervalos: ");
        for(String s:INTERVALS) System.out.print(s + "  ");
        System.out.println();
        String period = checkIfInArray(INTERVALS);
        queries.query3d(period,amount);
    }

    private static void optionsMenuDisplay() {
        System.out.println();
        System.out.println("Gestão de manutenção de activos físicos");
        System.out.println("1. Novo Ativo (a)");
        System.out.println("2. Substituir um elemento de equipa (b)");
        System.out.println("3. Colocar Ativo Fora de Serviço (c)");
        System.out.println("4. Custo Total Ativo (d)");
        System.out.println("5. Pessoas que estão a realizar a intervenção na “válvula de ar condicionado“ ou que gerem esse ativo (2d)");
        System.out.println("6. Ativos geridos ou intervencionados por “Manuel Fernandes” (2e)");
        System.out.println("7. Responsáveis de equipa que são (ou foram) gestores de pelo menos um ativo (3c)");
        System.out.println("8. Intervenções programadas para daqui a um mês (3d)");
        System.out.println("9. Sair");
    }

    private static void checkAndFixRestrictions() throws SQLException{
        //todas as equipas têm no mínimo 2 pessoas
        //esta restrição não é corrigida pelo código devido à complexidade da mesma e por não ser possível corrigir la na íntegra
        // a menos que realizássemos uma inserção de 1 ou mais elementos na DB
        if (!queries.checkEquipasMin2Elements()) System.out.println("does not comply");
        //VCOMERCIAL.dtvcomercial de VCOMERCIAL >= ACTIVO.dtaquisicao
        queries.checkRestrictionVComercial();
        //Se INTERVENCAO.valcusto > valor comercial do activo,
        // ACTIVO.estado = “0”, INTERVENCAO.estado = “concluído” e INTERVENCAO.dtfim = data actual
        queries.checkRestrictionIntervencao();
        //Se INTERVENCAO.dtfim é não nulo, estado = “concluído”;
        queries.checkRestrictionIntervencaoDtFim();
        //os ativos “filhos” são do mesmo tipo que o ativo “pai”;
        queries.checkRestrictionAtivoHierarchy();
        //a pessoa que gere um ativo não faz a manutenção desse ativo.
        queries.checkRestrictionConflictGestaoMan();
    }

    public static void printTable(ResultSet rs, int columnsNumber, String header) throws SQLException {
        int numRows = 1;
        if(rs.next()){
            System.out.print("     ");
            String[] headers = header.split(" ",columnsNumber+1);
            for(int i = 0 ; i <= columnsNumber; i++)System.out.print(headers[i] + "  ");
            System.out.println();
        }
        do{
            for(int i = 1 ; i <= columnsNumber; i++){
                if(i==1) System.out.print(numRows + " >  ");
                System.out.print(rs.getString(i) + "     "); //Print one element of a row
            }System.out.println();//Move to the next line to print the next row.
            numRows++;
        }while (rs.next());
        if(numRows == 1) System.out.println("Não existem valores para a interrogação efetuada");
    }

    //obter valores inseridos pelo utilizador
    private static int getValInt(){
        System.out.print("> ");
        int val = input.nextInt();
        //consume rest of line
        input.nextLine();
        return val;
    }

    //obter valores inseridos pelo utilizador
    private static String getValString(){
        System.out.print("> ");
        return input.nextLine();
    }

    //exit app
    private static void exit() throws SQLException {
        System.out.println("Confirma Saída do Programa");
        if(checkConsent()) System.exit(0);
        else optionsMenu();
    }

    private static boolean checkConsent() {
        System.out.println("prima S para confirmar ou qualquer outra tecla para cancelar");
        char confirmExit = input.next().charAt(0);
        input.nextLine();
        return (confirmExit =='s' || confirmExit =='S');
    }

    private static void printList(List<String> list) {
        for (String s : list) { System.out.print(s + "   "); }
        System.out.println();
    }

    //obter e verificar valores de data inserida por utilizador
    private static String getDate(){
        System.out.println("Ano");
        int year = checkBetweenBoundaries(1950,2100);
        System.out.println("Mês");
        System.out.println("Entre 1 e 12");
        System.out.println("1 = JAN  12 = DEZ");
        int month;
        if(year == CURRENT_YEAR ) month = checkBetweenBoundaries(CURRENT_MONTH, 12);
        else month = checkBetweenBoundaries(1, 12);
        System.out.println("Dia");
        int lastdaymonth = lastDayMonth(month, year);
        System.out.println("Entre 1 e " + lastdaymonth);
        int day = checkBetweenBoundaries(1, lastdaymonth);
        return getStringDate(year,month,day);
    }

    //obter ultimo dia de cada mês
    public static int lastDayMonth(int month, int year){
        int lastDay = -1;
        int[] lastDayArray = {-1, 31, -1, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};
        if(month == 2) {
            Calendar testCal = Calendar.getInstance();
            testCal.set(Calendar.YEAR, year);
            if (cal.getActualMaximum(Calendar.DAY_OF_YEAR) > 365) lastDay = 29;
            else lastDay = 28;
        }
        if(month != 2) lastDay = lastDayArray[month];
        return lastDay;
    }

    //passar dados de data para uma String dde formato Date de PSQL
    public static String getStringDate(int year, int month, int day){
        String date = year + "-";
        if(checkIfBelowMax(month,10)) date += month + "-";
        else date += "0" + month + "-";
        if(checkIfBelowMax(day,10)) date += day;
        else date += "0" + day;
        return date;
    }

    public static int checkBetweenBoundaries(int min, int max) {
        int var;
        do{var = getValInt();
        }while(var < min || var > max);
        return var;
    }

    public static boolean checkIfBelowMax(int var, int max) {return var >= max;}

    public static String checkIfInArray(String[] array){
        String var;
        do{
            var = getValString();
        }while (!checkIfInArray(var, array));
        return var;
    }

    public static boolean checkIfInArray(String var, String[] array){
        for (String s : array) if (s.equals(var)) return true;
        return false;
    }

    public static String checkIfInList(List<String> list){
        String var;
        do{var = getValString();
        }while (!list.contains(var));
        return var;
    }
}
