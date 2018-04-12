package pl.pwn.reaktor.ankieta.controller;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.mail.MessagingException;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.swing.JFileChooser;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.input.MouseEvent;
import javafx.stage.FileChooser;
import pl.pwn.reaktor.ankieta.mail.MailService;

public class AnkietaController {

    @FXML
    private MenuItem m_send;

    @FXML
    private MenuItem m_save_to_file;

    @FXML
    private MenuItem m_save_to_database;

    @FXML
    private MenuItem m_clear;

    @FXML
    private MenuItem m_close;

    @FXML
    private MenuItem m_about;

    @FXML
    private TextField tf_name;

    @FXML
    private TextField tf_last_name;

    @FXML
    private TextField tf_mail;

    @FXML
    private TextField tf_phone;

    @FXML
    private CheckBox cb_java;

    @FXML
    private CheckBox cb_python;

    @FXML
    private CheckBox cb_other;

    @FXML
    private TextField tf_other;

    @FXML
    private RadioButton rb_basic;

    @FXML
    private ToggleGroup g1;

    @FXML
    private RadioButton rb_intermediate;

    @FXML
    private RadioButton rb_advanced;

    @FXML
    private Button btn_preview;

    @FXML
    private ComboBox<String> cmb_courses;

    @FXML
    private TextArea ta_preview;
  

    @FXML
    void about(ActionEvent event) {
    		Alert about = new Alert(AlertType.INFORMATION);
    		about.setTitle("About");
    		about.setHeaderText("Instruction");
    		about.setContentText("Insert your info into the form");
    		about.show();
    }

    @FXML
    void clear(ActionEvent event) {
    		tf_name.clear();
    		tf_last_name.clear();
    		tf_mail.clear();
    		tf_phone.clear();
    		cb_java.setSelected(false);
    		cb_python.setSelected(false);
    		cb_other.setSelected(false);
    		tf_other.clear();
    		rb_intermediate.setSelected(true);
    		cmb_courses.setValue(null);
    		ta_preview.clear();
    		
    }

    @FXML
    void close(ActionEvent event) {
    		System.exit(0);
    }

    @FXML
    void preview(MouseEvent event) {
    	
    		if (isNotCompleted()) {
    			
    			//komunikat dla użytkownika, gdy pola są niewypełnione
    			showAlertFormNotCompleted();
    	
    		} else {
    			
    			//wstawienie danych do pola textArea
        		String preview = getFormData();
        		ta_preview.setText(preview);
    		}
    }

	private void showAlertFormNotCompleted() {
		Alert alert = new Alert(AlertType.WARNING);
		alert.setHeaderText("Warning");
		alert.setTitle("Błąd wyświetlania danych ankiety");
		alert.setContentText("Należy uzupełnić wszystkie pola");
		alert.show();
	}

	private String getFormData() {
		
		String name = tf_name.getText();
		String lastName = tf_last_name.getText();
		String mail = tf_mail.getText();
		String phone = tf_phone.getText();
		
		String java = cb_java.isSelected() ? "\n- " + cb_java.getText() : "";
		String python = cb_python.isSelected() ? "\n- " + cb_python.getText() : "";
		String other = cb_other.isSelected() ? "\n- " + tf_other.getText() : "";
		
		String language = rb_intermediate.getText();
		if (rb_basic.isSelected()) {
			language = rb_basic.getText();
		}
		if (rb_advanced.isSelected()) {
			language = rb_advanced.getText();
		}
		
		String course = cmb_courses.getValue();
		
		return "Kontakt: \n" + name + " " + lastName
				+ "\nE-mail: " + mail
				+ "\nTelefon: " + phone
				+ "\nJęzyki programowania:" + java + python + other
				+ "\nPoziom: " + language
				+ "\nKurs: " + course;
	}

	private boolean isNotCompleted() {
		return "".equals(tf_name.getText()) 
				|| "".equals(tf_last_name.getText()) 
				|| "".equals(tf_mail.getText()) 
				|| "".equals(tf_phone.getText()) 
				|| !(cb_java.isSelected() 
				|| cb_python.isSelected() 
				|| cb_other.isSelected()) 
				|| Objects.isNull(cmb_courses.getValue());
	}
	
	

    @FXML
    void saveToDatabase(ActionEvent event) {

    }

    @FXML
    void saveToFile(ActionEvent event) {
    		
    		if (isNotCompleted()) {
			showAlertFormNotCompleted();
		} else {
				//To nie działa na macu :(
				/*JFileChooser fileChooser = new JFileChooser();
				fileChooser.showOpenDialog(null);
				String choice = fileChooser.getSelectedFile().getPath();
				*/
			
			FileChooser fileChooser = new FileChooser();
    			File file = fileChooser.showOpenDialog(null);
    			String choice = file.getPath();
			try {
				PrintWriter zapis = new PrintWriter(choice);
				zapis.println(getFormData());
				zapis.close();
			} catch (FileNotFoundException e) {
				Alert error = new Alert(AlertType.ERROR);
				error.setTitle("Nie znaleziono pliku");
				error.setHeaderText("Błąd");
				error.setContentText(e.getMessage());
				error.show();
			}
		}

    }

    @FXML
    void send(ActionEvent event) {
    	if (isNotCompleted()) {
			showAlertFormNotCompleted();
		} else { 
			
			//https://howtodoinjava.com/regex/java-regex-validate-email-address/
			//regex101.com
			
			boolean isValidEmail = validateMail();
			
			if (isValidEmail) {
				Alert error = new Alert(AlertType.ERROR);
				error.setTitle("Błędny adres email");
				error.setHeaderText("Błąd");
				error.setContentText("Błędny adres email: " + tf_mail.getText() + "\nPodaj poprawny adres.");
				error.show();
				return; //wychodzi z metody - podobnie jak break
			}
			
			MailService mailService = new MailService();
			try {
				mailService.sendMail(tf_mail.getText(), "Ankieta Reaktora", getFormData());
				Alert successSend = new Alert(AlertType.CONFIRMATION);
				successSend.setTitle("Wysłano email na podany adres");
				successSend.setHeaderText("Sukces!");
				successSend.setContentText("Poszukaj maila w swojej skrzynce :)");
				successSend.show();
			} catch (MessagingException e) {
				Alert errorSend = new Alert(AlertType.ERROR);
				errorSend.setTitle("Nie udało się wysłać maila");
				errorSend.setHeaderText("Błąd");
				errorSend.setContentText(e.getMessage());
			}
		}
    }

	private boolean validateMail() {
		String regex = "^[a-zA-Z0-9_!#$%&’*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+$";
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(tf_mail.getText());
		boolean isValidEmail = !matcher.matches();
		return isValidEmail;
	}
    
    
    @FXML
    void unlockOther(MouseEvent event) {
    		
    		if (cb_other.isSelected()) {
    			tf_other.setDisable(false);
    		} else {
    			tf_other.clear();
    			tf_other.setDisable(true);
    		}
    }
    
    /**
     * Tworzenie listy do comboboxa
     */
    ObservableList<String> courses = FXCollections.observableArrayList("Back-end", "Front-end", "Web developer", "Tester");
    
    public void initialize() {
    		//dopiero po kliknięciu na combobox, zostaną zaczytane wartości
    		//lista jest ładowana dopiero, kiedy klikniemy
    		cmb_courses.setItems(courses);
    }
    

}
