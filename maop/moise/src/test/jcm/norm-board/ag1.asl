
!start.

+!start : true
   <- .print("hello world.");
      makeArtifact(nb1,"ora4mas.nopl.NormativeBoard",[],AId);
      focus(AId);
      debug(inspector_gui(on));
      load("e1.npl");
      addFact(b(13));
      //.wait(5000);
      //removeFact(b(3))
   .

+obligation(_,_,_,_)
   <- addFact(b(-1)).

+oblUnfulfilled(O) <- .print("Unfulfilled ",O).
