package prebot.brain.knowledge;

import prebot.common.code.Code.KnowledgeActionEvaluation;

public abstract class ActionKnowledge extends Knowledge {

	protected abstract void prevent();
	
	protected abstract boolean doSomething();

	protected abstract boolean doSomeOtherthing();

	protected abstract KnowledgeActionEvaluation evaulateAction();
	
	@Override
	protected void waiting() {
		super.waiting();
	}

	@Override
	protected void verifying() {
		prevent();
		super.verifying();
	}

	@Override
	protected void provedTrue() {
		boolean doSomethingContinue = doSomething();
		KnowledgeActionEvaluation evaluation = evaulateAction();

		if (!doSomethingContinue && evaluation != KnowledgeActionEvaluation.DONT_KNOW)
			super.provedTrue();
	}

	@Override
	protected void provedFalse() {
		boolean doSomeOtherthingContinue = doSomeOtherthing();

		if (!doSomeOtherthingContinue)
			super.provedFalse();
	}

	@Override
	protected void writeFile() {
		// TODO Auto-generated method stub
		super.writeFile();
	}
	
	
}
