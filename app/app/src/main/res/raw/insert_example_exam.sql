INSERT INTO exams (title, questions, is_finished) VALUES ('Math Exam - Basic Algebra', '1,2,3', 0);

INSERT INTO exams (title, questions, is_finished) VALUES ('Math Exam - Arithmetics', '4,5', 0);

INSERT INTO questions (type, question, assets, options, correct_answer)
VALUES
(1, 'What is 2 + 2?', 'null', '4,3,5,6', 0),
(2, 'True or False: 5 is greater than 3.', 'null', 'True,False', 0),
(1, 'Solve for x: 2x = 10', 'null', '2,10,5,20', 2);


INSERT INTO questions (type, question, assets, options, correct_answer)
VALUES
(2, 'True or False: 4 is lesser than 12.', 'null', 'True,False', 1),
(2, 'True or False: 5 is greater than 3.', 'null', 'True,False', 0);
