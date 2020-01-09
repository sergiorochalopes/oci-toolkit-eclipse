package com.oracle.oci.eclipse.ui.explorer.database;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWizard;

import com.oracle.bmc.database.model.AutonomousDatabaseSummary;
import com.oracle.oci.eclipse.sdkclients.ADBInstanceClient;

public class ScaleUpDownADBWizard  extends Wizard implements INewWizard {

    private ScaleUpDownADBWizardPage page;
    private ISelection selection;
    private AutonomousDatabaseSummary instance;

    public ScaleUpDownADBWizard(final AutonomousDatabaseSummary instance) {
        super();
        setNeedsProgressMonitor(true);
        this.instance = instance;
    }

    @Override
    public void addPages() {
        page = new ScaleUpDownADBWizardPage(selection, instance);
        addPage(page);
    }

    /**
     * This method is called when 'Finish' button is pressed in
     * the wizard. We will create an operation and run it
     * using wizard as execution context.
     */
    @Override
    public boolean performFinish() {
        final String cpuCoreCount = page.getCPUCoreCount();
        final String dataStorageSizeInTBs = page.getDataStorageInTBText();
        final Boolean isAutoScalingEnabled = page.isAutoScalingEnabled();
        IRunnableWithProgress op = new IRunnableWithProgress() {
            @Override
			public void run(IProgressMonitor monitor) throws InvocationTargetException {
				ADBInstanceClient.getInstance().scaleUpDownInstance(instance, Integer.parseInt(cpuCoreCount),
						Integer.parseInt(dataStorageSizeInTBs), isAutoScalingEnabled);
				monitor.done();
			}
        };
        try {
            getContainer().run(true, false, op);
        } catch (InterruptedException e) {
            return false;
        } catch (InvocationTargetException e) {
            Throwable realException = e.getTargetException();
            MessageDialog.openError(getShell(), "Failed to Scale Up/Down ADB instance : "+instance.getDbName(), realException.getMessage());
            return false;
        }

        return true;
    }

    /**
     * We will accept the selection in the workbench to see if
     * we can initialize from it.
     * @see IWorkbenchWizard#init(IWorkbench, IStructuredSelection)
     */
    @Override
    public void init(IWorkbench workbench, IStructuredSelection selection) {
        this.selection = selection;
    }

}