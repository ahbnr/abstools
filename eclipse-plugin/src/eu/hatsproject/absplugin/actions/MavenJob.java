package eu.hatsproject.absplugin.actions;

import static eu.hatsproject.absplugin.util.Constants.MAVEN_EXEC_PATH;
import static eu.hatsproject.absplugin.util.UtilityFunctions.getAbsNature;
import static eu.hatsproject.absplugin.util.UtilityFunctions.getDefaultPreferenceStore;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.jobs.Job;

import eu.hatsproject.absplugin.builder.AbsNature;
import eu.hatsproject.absplugin.console.ConsoleManager.MessageType;
import eu.hatsproject.absplugin.console.MsgConsole;
import eu.hatsproject.absplugin.exceptions.AbsJobException;
import eu.hatsproject.absplugin.exceptions.NoABSNatureException;

/**
 * 
 * @author pwong
 *
 */
public class MavenJob extends Job {

	private final String goal = "eu.hats-project:abs-maven-plugin:1.0-SNAPSHOT:configs";
	private final String pom = "pom.xml";
	private final IProject project;
	private boolean abort;
	private Process process;
	
	public MavenJob(IProject project) {
		super("Maven Job");
		this.project = project;
		setUser(true);
	}

	private String getMavenPath() {
		return getDefaultPreferenceStore().getString(MAVEN_EXEC_PATH);
	}
	
	private IFile getPom() {
		return project.getFile(pom);
	}
	
	@Override
	protected void canceling() {
		abort = true;
		if(process != null){
			process.destroy();
		}
		super.cancel();
	}
	
	public void runMavenUpdates() throws NoABSNatureException, AbsJobException, IOException {
		AbsNature nature = getAbsNature(project);
		if(nature == null){
			throw new NoABSNatureException();
		}
		
		IFile pom = null;
		if (! (pom = getPom()).exists()) {
			throw new AbsJobException("POM for this project is not defined");
		}
		
		MsgConsole console = nature.getMavenConsole();
		console.clear();
		
		List<String> args = new ArrayList<String>();
		
		File file = new File(getMavenPath());
		if (! file.exists() || !file.isDirectory()) {
			throw new AbsJobException("Maven path is not defined");
		}
		
		String command;
		if(Platform.getOS().equals(Platform.OS_WIN32)){
			command = new File(file,"bin\\mvn.bat").getAbsolutePath();
 		} else {
 			command = new File(file,"bin/mvn").getAbsolutePath();
 		}
		
		args.add(command);
		args.add("-f");
		args.add(pom.getLocation().toFile().getAbsolutePath());
		args.add(goal);
		
		InputStream ins = null;
		OutputStream outs = null;
		try {
			if(!abort) process = Runtime.getRuntime().exec(args.toArray(new String[args.size()]));
			ins = process.getInputStream();
			outs = console.getOutputStream(MessageType.MESSAGE_INFO);
		
			int d = 0;
			while ((d = ins.read()) != -1) {
				outs.write(d);
			}
		} finally {
			if (ins != null) ins.close();
			if (outs != null) outs.close();
		}
	}

	protected IStatus run(IProgressMonitor monitor) {
		return null;
	}

}
