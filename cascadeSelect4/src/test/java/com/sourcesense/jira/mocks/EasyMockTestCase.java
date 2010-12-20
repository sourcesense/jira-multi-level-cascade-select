package com.sourcesense.jira.mocks;

import org.easymock.MockControl;

public abstract class EasyMockTestCase extends JiraTestCase
{
    // ------------------------------------------------------------------------------------------ Replay & Reset Methods


    protected void _startTestPhase()
    {
        for (int i = 0; i < _getRegisteredMockControllers().length; i++)
        {
            MockControl mockControl = _getRegisteredMockControllers()[i];
            mockControl.replay();
        }
    }

    protected void _reset()
    {
        for (int i = 0; i < _getRegisteredMockControllers().length; i++)
        {
            MockControl mockControl = _getRegisteredMockControllers()[i];
            mockControl.reset();
        }
    }

    protected void _verifyAll()
    {
        for (int i = 0; i < _getRegisteredMockControllers().length; i++)
        {
            MockControl mockControl = _getRegisteredMockControllers()[i];
            mockControl.verify();
        }
    }

    public abstract MockControl[] _getRegisteredMockControllers();
}