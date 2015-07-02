/*
 * DBeaver - Universal Database Manager
 * Copyright (C) 2010-2015 Serge Rieder (serge@jkiss.org)
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License (version 2)
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
package org.jkiss.dbeaver.ui.actions.datasource;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.handlers.HandlerUtil;
import org.jkiss.dbeaver.core.DBeaverUI;
import org.jkiss.dbeaver.model.DBUtils;
import org.jkiss.dbeaver.model.exec.*;
import org.jkiss.dbeaver.model.runtime.DBRProgressMonitor;
import org.jkiss.dbeaver.model.runtime.DBRRunnableWithProgress;
import org.jkiss.dbeaver.ui.UIUtils;
import org.jkiss.dbeaver.ui.actions.AbstractDataSourceHandler;

import java.lang.reflect.InvocationTargetException;

public class DataSourceRollbackHandler extends AbstractDataSourceHandler
{
    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException
    {
        DBCExecutionContext context = getExecutionContext(event, true);
        if (context != null && context.isConnected()) {
            execute(HandlerUtil.getActiveShell(event), context);
        }
        return null;
    }

    public static void execute(Shell shell, final DBCExecutionContext context) {
        try {
            DBeaverUI.runInProgressService(new DBRRunnableWithProgress() {
                @Override
                public void run(DBRProgressMonitor monitor)
                    throws InvocationTargetException, InterruptedException {
                    DBCTransactionManager txnManager = DBUtils.getTransactionManager(context);
                    if (txnManager != null) {
                        DBCSession session = context.openSession(monitor, DBCExecutionPurpose.UTIL, "Rollback transaction");
                        try {
                            txnManager.rollback(session, null);
                        } catch (DBCException e) {
                            throw new InvocationTargetException(e);
                        } finally {
                            session.close();
                        }
                    }
                }
            });
        } catch (InvocationTargetException e) {
            UIUtils.showErrorDialog(shell, "Rollback", "Error during session rollback", e);
        } catch (InterruptedException e) {
            // do nothing
        }
    }

}