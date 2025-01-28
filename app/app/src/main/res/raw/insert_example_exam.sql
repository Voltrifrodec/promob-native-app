INSERT INTO exams (title, questions, is_finished) VALUES ('Math Exam - Basic Algebra', '1,2,3', 0);

INSERT INTO questions (type, question, assets, options, correct_answer)
VALUES
(1, 'What is 2 + 2?', NULL, '4,3,5,6', 0),
(2, 'True or False: 5 is greater than 3.', NULL, 'True,False', 0),
(1, 'Solve for x: 2x = 10', NULL, '2,10,5,20', 2);