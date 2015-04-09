package br.com.fortium.bibliorium.managedbean;

import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import br.com.fortium.bibliorium.builder.EmprestimoBuilder;
import br.com.fortium.bibliorium.enumeration.DialogType;
import br.com.fortium.bibliorium.persistence.entity.Copia;
import br.com.fortium.bibliorium.persistence.entity.Emprestimo;
import br.com.fortium.bibliorium.persistence.entity.Usuario;
import br.com.fortium.bibliorium.persistence.enumeration.EstadoCopia;
import br.com.fortium.bibliorium.persistence.enumeration.EstadoUsuario;
import br.com.fortium.bibliorium.print.ComprovanteEmprestimoPrintable;
import br.com.fortium.bibliorium.print.Printable;
import br.com.fortium.bibliorium.print.PrintableBuilder;
import br.com.fortium.bibliorium.print.PrintableDataHolder;
import br.com.fortium.bibliorium.service.CopiaService;
import br.com.fortium.bibliorium.service.EmprestimoService;
import br.com.fortium.bibliorium.service.UsuarioService;

@ManagedBean
@ViewScoped
public class GerenciarEmprestimoMB extends AbstractManagedBean<GerenciarEmprestimoMB> {

	private enum Action {EMPRESTIMO, RESERVA, DEVOLUCAO}
	
	private static final long serialVersionUID = 2249645974635438267L;
	
	private String codCopia;
	private String cpf;
	
	private Copia copia;
	private Usuario leitor;
	
	private Boolean displayBuscaLeitor;
	
	private Action action;
	
	@EJB
	private CopiaService copiaService;
	
	@EJB
	private EmprestimoService emprestimoService;
	
	@EJB
	private UsuarioService usuarioService;
	
	@Override
	protected void init() {
		
	}
	
	public void buscarCopia(){
		Long idCopia = null;
		
		try{
			idCopia = Long.parseLong(codCopia);
			copia = copiaService.buscar(idCopia);
		}catch(NumberFormatException e){
			copia = null;
		}
	}
	
	public void buscarLeitor(){
		leitor = usuarioService.buscar(cpf);
	}
	
	public void emprestar(){
		setDisplayBuscaLeitor(Boolean.TRUE);
		setAction(Action.EMPRESTIMO);
	}
	
	public void reservar(){
		setDisplayBuscaLeitor(Boolean.TRUE);
		setAction(Action.RESERVA);
	}
	
	public void emprestarReserva(){
		Emprestimo reserva = emprestimoService.buscarReserva(copia);
		leitor = reserva.getUsuario();
		cpf    = leitor.getCpf();
		
		emprestimoService.concluirEmprestimo(reserva);
		
		setAction(Action.EMPRESTIMO);
	}
	
	public void confirmar(){
		if(getAction() == Action.EMPRESTIMO || getAction() == Action.RESERVA){
			efetuarEmprestimo();
		}
	}

	private void efetuarEmprestimo() {
		if(leitor.getEstado() == EstadoUsuario.INADIMPLENTE){
			getDialogUtil().showDialog(DialogType.ERROR, "Empr�stimo/Reserva recusado(a), este leitor est� inadimplente");
			return;
		}else if(emprestimoService.countEmprestimoAtivos(leitor) >= 5){
			getDialogUtil().showDialog(DialogType.ERROR, "Empr�stimo/Reserva recusado(a), este leitor j� atingiu o limite de 5 empr�stimo/reserva ativos");
			return;
		}
		
		Emprestimo emprestimo = null;
		
		switch(getAction()){
			case EMPRESTIMO:
				emprestimo = EmprestimoBuilder.novoEmprestimo(leitor, copia);
				emprestimoService.efetuarEmprestimo(emprestimo);
				getDialogUtil().showDialog(DialogType.SUCCESS, "Empr�stimo realizado com sucesso");
				break;
			case RESERVA:
				emprestimo = EmprestimoBuilder.novaReserva(leitor, copia);
				emprestimoService.efetuarEmprestimo(emprestimo);
				getDialogUtil().showDialog(DialogType.SUCCESS, "Reserva realizada com sucesso");
				break;
			default:
				return;
		}
		
		printComprovanteEmprestimo(emprestimo);
		reset();
	}

	public void reset(){
		setCpf(null);
		setCopia(null);
		setLeitor(null);
		setCodCopia(null);
		setDisplayBuscaLeitor(Boolean.FALSE);
	}
	
	private void printComprovanteEmprestimo(Emprestimo emprestimo){
		Printable comprovante = PrintableBuilder.buildComprovanteEmprestimo(emprestimo);
		PrintableDataHolder dataHolder = new PrintableDataHolder(ComprovanteEmprestimoPrintable.NAME, comprovante);
		setPrintable(dataHolder);
	}
	
	public boolean isDisponivel(){
		if(copia == null){
			return Boolean.FALSE;
		}else{
			return copia.getEstado() == EstadoCopia.DISPONIVEL;
		}
	}
	
	public boolean isEmprestada(){
		if(copia == null){
			return Boolean.FALSE;
		}else{
			return copia.getEstado() == EstadoCopia.EMPRESTADA;
		}
	}
	
	public boolean isReservada(){
		if(copia == null){
			return Boolean.FALSE;
		}else{
			return copia.getEstado() == EstadoCopia.RESERVADA;
		}
	}
	
	public Copia getCopia() {
		return copia;
	}

	public void setCopia(Copia copia) {
		this.copia = copia;
	}

	public String getCodCopia() {
		return codCopia;
	}

	public void setCodCopia(String codCopia) {
		this.codCopia = codCopia;
	}

	public Boolean getDisplayBuscaLeitor() {
		return displayBuscaLeitor;
	}

	public void setDisplayBuscaLeitor(Boolean displayBuscaLeitor) {
		this.displayBuscaLeitor = displayBuscaLeitor;
	}

	public String getCpf() {
		return cpf;
	}

	public void setCpf(String cpf) {
		this.cpf = cpf;
	}

	public Usuario getLeitor() {
		return leitor;
	}

	public void setLeitor(Usuario leitor) {
		this.leitor = leitor;
	}

	public Action getAction() {
		return action;
	}

	public void setAction(Action action) {
		this.action = action;
	}

}
