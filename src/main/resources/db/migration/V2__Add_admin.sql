insert into usr(id, user_name, password, active)
	values(1, 'admin', 'pass', true);

insert into user_role(user_id, roles)
	values(1, 'USER'), (1, 'ADMIN');