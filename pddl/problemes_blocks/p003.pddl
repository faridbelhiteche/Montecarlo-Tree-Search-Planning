(define (problem BLOCKS-4-2)
(:domain BLOCKS)
(:objects A B C D - block)
(:INIT (CLEAR D) (ONTABLE A) (ON B A) (ON C B) (ON D C) (HANDEMPTY))
(:goal (AND (ON C B) (ON B A) (ON A D))))
