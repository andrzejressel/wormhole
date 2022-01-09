package pl.andrzejressel.prompt.model

import java.nio.file.Paths

object ConsoleReducer extends ((ConsoleState, ConsoleEvent) => ConsoleState) {

  override def apply(v1: ConsoleState, v2: ConsoleEvent): ConsoleState =
    v2 match {
      case NewConsole(pid)     => v1.copy(pid = Some(pid))
      case ChangeDir(dir)      => v1.copy(currentDirectory = Some(Paths.get(dir)))
      case SetEnvironment(env) =>
        // SortedMap mostly for nicer logs
        v1.copy(env = env.to(collection.immutable.SortedMap))
    }

}
