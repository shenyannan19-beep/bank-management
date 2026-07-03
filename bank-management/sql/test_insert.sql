DELETE FROM TransactionRecord;
DELETE FROM Loan;
DELETE FROM Account;
DELETE FROM Employee;
DELETE FROM Customer;
INSERT INTO Customer (customer_id, customer_name, id_card, phone, city, address, open_date) VALUES
('C00001', N'张伟', '110101199001011234', '13800001001', N'北京', N'北京市朝阳区建国路100号', '2020-03-15'),
('C00002', N'李娜', '110102199105152345', '13800001002', N'北京', N'北京市海淀区中关村大街1号', '2020-05-20');
INSERT INTO Employee (employee_id, employee_name, position, department, phone, hire_date, branch) VALUES
('T0001', N'陈建国', N'行长', N'管理层', '13900010001', '2018-01-10', N'北京总行'),
('T0002', N'王建国', N'副行长', N'管理层', '13900010002', '2018-03-15', N'北京总行');
