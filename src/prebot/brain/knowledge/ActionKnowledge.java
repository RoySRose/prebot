package prebot.brain.knowledge;

import prebot.common.code.Code.KnowledgeActionEvaluation;

public abstract class ActionKnowledge extends Knowledge {

	protected abstract void prevent();

	protected abstract boolean doSomething();

	protected abstract boolean doSomeOtherthing();

	protected abstract KnowledgeActionEvaluation evaulateAction();

	@Override
	protected boolean waiting() {
		return super.waiting();
	}

	@Override
	protected boolean verifying() {
		prevent();
		return super.verifying();
	}

	@Override
	protected boolean provedTrue() {
		boolean doSomethingContinue = doSomething();
		KnowledgeActionEvaluation evaluation = evaulateAction();

		if (!doSomethingContinue && evaluation != KnowledgeActionEvaluation.DONT_KNOW) {
			return super.provedTrue();
		} else {
			return false;
		}
	}

	@Override
	protected boolean provedFalse() {
		boolean doSomeOtherthingContinue = doSomeOtherthing();

		if (!doSomeOtherthingContinue) {
			return super.provedFalse();
		} else {
			return false;
		}
	}

	@Override
	protected boolean writeFile() {
		// TODO Auto-generated method stub
		return super.writeFile();
	}

}
