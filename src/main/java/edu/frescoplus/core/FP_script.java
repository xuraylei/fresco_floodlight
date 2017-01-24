package edu.frescoplus.core;

//FRESCO scripts

public class FP_script {
	public String name;
	public String description;
	public int moduleNum;

	public Module[] modules;

	public String getName() {
		return name;
	}

	public Module[] getModules() {
		return modules;
	}

	public String toString() {
		StringBuilder script_str = new StringBuilder();

		if (moduleNum == 0) {
			return "No modules in the script!";
		}

		script_str.append("The name of the script is " + name + "\n");
		script_str.append("The number of modules in the script is " + moduleNum
				+ "\n");

		for (int i = 0; i < modules.length; i++) {
			script_str.append("The " + (i + 1) + "th module:\n");
			script_str.append(modules[i].toString());
		}

		return script_str.toString();
	}
}

class Module {
	private String id; // id of module
	private String type; // type of module
	private String event; // the trigger events of module (only support a single event)
	private String[] parameters; // the initial configurations of module
	private String[] inputs; // the input specification of module

	public String getID(){
		return this.id;
	}
	
	public String getType() {
		return this.type;
	}

	public String getEvent() {
		return event;
	}

	public String[] getParameters() {
		return parameters;
	}

	public String[] getInputs() {
		return inputs;
	}

	public String toString() {
		StringBuilder module_str = new StringBuilder();

		module_str.append("The name of the module is " + this.type + "\n");
		module_str.append("The event of the module is " + this.event + "\n");

		if (parameters.length == 0) {
			module_str.append("No parameter for this module.");
		}

		for (int i = 0; i < parameters.length; i++) {
			module_str.append("The " + (i + 1) + "th parameter is "
					+ this.parameters[i] + "\n");
		}

		if (inputs.length == 0) {
			module_str.append("No input for this module.");
		}

		for (int i = 0; i < inputs.length; i++) {
			module_str.append("The " + (i + 1) + "th input is " + this.inputs[i]
					+ "\n");
		}

		return module_str.toString();

	}

}
